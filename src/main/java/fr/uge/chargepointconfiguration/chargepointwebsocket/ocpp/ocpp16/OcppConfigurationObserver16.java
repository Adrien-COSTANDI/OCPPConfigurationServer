package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ChargePointManager;
import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppMessageSender;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppObserver;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16.data.FirmwareStatus;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16.data.RegistrationStatus;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16.data.ResetType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp2.BootNotificationResponse20;
import fr.uge.chargepointconfiguration.configuration.ConfigurationTranscriptor;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLog;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLog;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import fr.uge.chargepointconfiguration.typeallowed.TypeAllowed;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * Defines the OCPP 1.6 observer.<br>
 * An observer is defined by the interface {@link OcppObserver}.
 */
public class OcppConfigurationObserver16 implements OcppObserver {
  private final OcppMessageSender sender;
  private final ChargePointManager chargePointManager;
  private final ChargepointRepository chargepointRepository;
  private final FirmwareRepository firmwareRepository;
  private final Queue<ChangeConfigurationRequest16> queue = new LinkedList<>();
  private final CustomLogger logger;
  private String firmwareVersion;
  private String targetFirmwareVersion;
  private boolean lastOrderSent = false;


  /**
   * {@link OcppConfigurationObserver16}'s constructor.
   *
   * @param sender                {@link OcppMessageSender}.
   * @param chargePointManager    {@link ChargePointManager}.
   * @param chargepointRepository {@link ChargepointRepository}.
   * @param firmwareRepository    {@link FirmwareRepository}.
   * @param logger                {@link CustomLogger}.
   */
  public OcppConfigurationObserver16(OcppMessageSender sender,
                                     ChargePointManager chargePointManager,
                                     ChargepointRepository chargepointRepository,
                                     FirmwareRepository firmwareRepository,
                                     CustomLogger logger) {
    this.sender = Objects.requireNonNull(sender);
    this.chargePointManager = Objects.requireNonNull(chargePointManager);
    this.chargepointRepository = Objects.requireNonNull(chargepointRepository);
    this.firmwareRepository = Objects.requireNonNull(firmwareRepository);
    this.logger = Objects.requireNonNull(logger);
  }

  @Override
  public void onMessage(OcppMessage ocppMessage) {
    Objects.requireNonNull(ocppMessage);
    switch (ocppMessage) {
      case BootNotificationRequest16 b -> processBootNotification(b);
      case ChangeConfigurationResponse16 c -> processConfigurationResponse(c);
      case ResetResponse16 ignored -> processResetResponse();
      case FirmwareStatusNotificationRequest16 f -> processFirmwareStatusResponse(f);
      default -> {
        // ignore
      }
    }
  }

  @Override
  public void onConnection(ChargePointManager chargePointManager) {
    Objects.requireNonNull(chargePointManager);
    // TODO : Is this method really useful ?
  }

  @Override
  public void onDisconnection(ChargePointManager chargePointManager) {
    if (chargePointManager.getCurrentChargepoint() == null) {
      var reset = new ResetRequest16(ResetType.Hard);
      sender.sendMessage(reset, chargePointManager);
    }
  }

  /**
   * Processes the received {@link BootNotificationRequest16} sent by the chargepoint.<br>
   * It is here where we start the automation of the configuration/firmware update.
   *
   * @param bootNotificationRequest16 {@link BootNotificationRequest16}.
   */
  private void processBootNotification(
          BootNotificationRequest16 bootNotificationRequest16) {
    firmwareVersion = bootNotificationRequest16.firmwareVersion();
    // Get charge point from database
    chargePointManager.setCurrentChargepoint(
            chargepointRepository.findBySerialNumberChargepointAndConstructor(
                    bootNotificationRequest16.chargePointSerialNumber(),
                    bootNotificationRequest16.chargePointVendor()
            ));
    // If charge point is not found then skip it
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (currentChargepoint == null) {
      logger.warn(new TechnicalLog(TechnicalLogEntity.Component.BACKEND,
              "unknown chargepoint, serial number : "
                      + bootNotificationRequest16.chargePointSerialNumber()));
      var response = new BootNotificationResponse20(
              LocalDateTime.now().toString(),
              10,
              RegistrationStatus.Rejected
      );
      sender.sendMessage(response, chargePointManager);
      return;
    }
    var config = currentChargepoint.getConfiguration();
    targetFirmwareVersion = config.getFirmware().getVersion();
    currentChargepoint.setState(true);
    currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PENDING);
    logger.info(new BusinessLog(null,
            currentChargepoint,
            BusinessLogEntity.Category.LOGIN,
            "chargepoint ("
                    + currentChargepoint.getSerialNumberChargepoint()
                    + ") is authenticated"));
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    // Send BootNotification Response
    var response = new BootNotificationResponse16(
            LocalDateTime.now().toString(),
            5,
            RegistrationStatus.Accepted
    );
    sender.sendMessage(response, chargePointManager);
    switch (currentChargepoint.getStep()) {
      case Chargepoint.Step.CONFIGURATION -> processConfigurationRequest();
      case Chargepoint.Step.FIRMWARE -> processFirmwareRequest();
      default -> {
        // ignore
      }
    }
  }

  /**
   * Processes the configuration.<br>
   * It searches in database the configuration for the chargepoint,
   * loads the key-value and sends a {@link ChangeConfigurationRequest16} for the chargepoint.<br>
   */
  @SuppressWarnings("unchecked")
  private void processConfigurationRequest() {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (queue.isEmpty()) {
      // The change configuration list is empty, so we load the configuration
      var configuration = currentChargepoint.getConfiguration().getConfiguration();
      var mapper = new ObjectMapper();
      try {
        // Cast
        HashMap<String, String> configMap = mapper.readValue(configuration, HashMap.class);
        configMap.forEach((key, value) -> {
          var configMessage = new ChangeConfigurationRequest16(
                  ConfigurationTranscriptor.idToEnum(Integer.parseInt(key))
                          .getOcpp16Key()
                          .getFirmwareKeyAccordingToVersion(firmwareVersion),
                  value
          );
          queue.add(configMessage);
          logger.info(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.CONFIG,
                  "added configuration in the waiting list for the chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") : "
                          + configMessage));
        });
      } catch (JsonProcessingException e) {
        logger.error(new BusinessLog(null,
                currentChargepoint,
                BusinessLogEntity.Category.CONFIG,
                "couldn't read configuration for the chargepoint ("
                        + currentChargepoint.getSerialNumberChargepoint()
                        + ")"));
        return;
      }
    }
    var config = queue.poll();
    if (config == null && !lastOrderSent) {
      logger.info(new BusinessLog(null,
              currentChargepoint,
              BusinessLogEntity.Category.CONFIG,
              "configuration for the chargepoint ("
                      + currentChargepoint.getSerialNumberChargepoint()
                      + ") is almost done, sending last order ! "));
      var clientId = currentChargepoint.getClientId();
      var changeConfig = new ChangeConfigurationRequest16(
              ConfigurationTranscriptor.CHARGEPOINT_IDENTITY
                      .getOcpp16Key()
                      .getFirmwareKeyAccordingToVersion(firmwareVersion),
              clientId);
      queue.add(changeConfig);
      var finalServerAddress = System.getenv("FINAL_WS_SERVER_ADDRESS");
      if (finalServerAddress != null) {
        changeConfig = new ChangeConfigurationRequest16(
                ConfigurationTranscriptor.NETWORK_PROFILE
                        .getOcpp16Key()
                        .getFirmwareKeyAccordingToVersion(firmwareVersion),
                finalServerAddress);
        queue.add(changeConfig);
        lastOrderSent = true;
      }
      config = queue.poll();
      sender.sendMessage(config, chargePointManager);
    } else if (config == null) {
      logger.info(new BusinessLog(null,
              currentChargepoint,
              BusinessLogEntity.Category.CONFIG,
              "configuration for the chargepoint ("
                      + currentChargepoint.getSerialNumberChargepoint()
                      + ") is done ! "));
      currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.FINISHED);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
    } else {
      sender.sendMessage(config, chargePointManager);
      currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PROCESSING);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
    }
  }

  /**
   * Processes the {@link ChangeConfigurationResponse16} sent by the chargepoint.<br>
   * If the change has been rejected, we stop the update.<br>
   * Otherwise, we continue.
   *
   * @param response {@link ChangeConfigurationResponse16}.
   */
  private void processConfigurationResponse(ChangeConfigurationResponse16 response) {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    switch (response.status()) {
      case Accepted, RebootRequired -> {
        if (queue.isEmpty() && !lastOrderSent) {
          logger.info(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.CONFIG,
                  "configuration for the chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") is almost done, sending last order ! "));
          var clientId = currentChargepoint.getClientId();
          var changeConfig = new ChangeConfigurationRequest16(
                  ConfigurationTranscriptor.CHARGEPOINT_IDENTITY
                          .getOcpp16Key()
                          .getFirmwareKeyAccordingToVersion(firmwareVersion),
                  clientId);
          queue.add(changeConfig);
          var finalServerAddress = System.getenv("FINAL_WS_SERVER_ADDRESS");
          if (finalServerAddress != null) {
            changeConfig = new ChangeConfigurationRequest16(
                    ConfigurationTranscriptor.NETWORK_PROFILE
                            .getOcpp16Key()
                            .getFirmwareKeyAccordingToVersion(firmwareVersion),
                    ConfigurationTranscriptor.NETWORK_PROFILE
                            .getOcpp16Key()
                            .getValueFormatAccordingToVersion(firmwareVersion)
                            .formatted(finalServerAddress));
            queue.add(changeConfig);
          }
          lastOrderSent = true;
          processConfigurationRequest();
        } else if (queue.isEmpty()) {
          currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.FINISHED);
          chargepointRepository.save(currentChargepoint);
          // Dispatch information to users
          chargePointManager.notifyStatusUpdate();
          var reset = new ResetRequest16(ResetType.Hard);
          sender.sendMessage(reset, chargePointManager);
          logger.info(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.CONFIG,
                  "configuration for the chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") is done ! "));
        } else {
          processConfigurationRequest();
        }
      }
      default -> {
        logger.warn(new BusinessLog(null,
                currentChargepoint,
                BusinessLogEntity.Category.CONFIG,
                "configuration for the chargepoint ("
                        + currentChargepoint.getSerialNumberChargepoint()
                        + ") has failed, see its status ! "));
        currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.FAILED);
        currentChargepoint.setError(response.status().name());
        chargepointRepository.save(currentChargepoint);
        // Dispatch information to users
        chargePointManager.notifyStatusUpdate();
        queue.clear();
      }
    }
  }

  /**
   * Processes the firmware.<br>
   * It searches in database the firmware target for the chargepoint.<br>
   * We upgrade the firmware step by step, meaning,
   * we choose the closest firmware to the current firmware.<br>
   * Example :<br>
   * The firmware target is 5.8.7, and the current target is 5.4.8.<br>
   * We upgrade to 5.5 > 5.6 > 5.7 > 5.8.
   */
  private void processFirmwareRequest() {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    var firmware = currentChargepoint.getConfiguration().getFirmware();
    logger.info(new BusinessLog(null,
            currentChargepoint,
            BusinessLogEntity.Category.FIRM,
            "firmware target ("
                    + firmware.getVersion()
                    + ") for chargepoint ( "
                    + currentChargepoint.getSerialNumberChargepoint()
                    + ")"));
    var typesAllowed = firmware.getTypesAllowed();
    var link = "";
    for (var typeAllowed : typesAllowed) {
      if (typeAllowed.getType().equals(currentChargepoint.getType())
              && typeAllowed.getConstructor().equals(currentChargepoint.getConstructor())) {
        link = fetchUrlFromFirstCompatibleVersion(typeAllowed);
        break;
      }
    }
    if (link.isEmpty()) {
      logger.info(new BusinessLog(null,
              currentChargepoint,
              BusinessLogEntity.Category.FIRM,
              "firmware update for the chargepoint ("
                      + currentChargepoint.getSerialNumberChargepoint()
                      + ") is done ! "));
      currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PENDING);
      currentChargepoint.setStep(Chargepoint.Step.CONFIGURATION);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      processConfigurationRequest();
      return;
    }
    currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PROCESSING);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    var firmwareRequest = new UpdateFirmwareRequest16(
            link, LocalDateTime.now().toString());
    sender.sendMessage(firmwareRequest, chargePointManager);
  }

  /**
   * Fetches the first compatible firmware version from the database.
   *
   * @param typeAllowed {@link TypeAllowed}.
   * @return The URL for downloading the firmware.
   */
  private String fetchUrlFromFirstCompatibleVersion(TypeAllowed typeAllowed) {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    var comparison = targetFirmwareVersion.compareTo(firmwareVersion);
    if (comparison > 0) {
      var firmwares = firmwareRepository
              .findAllByTypeAllowedAsc(typeAllowed);
      for (var firmware : firmwares) {
        if (firmware.getVersion().compareTo(firmwareVersion) > 0) {
          logger.info(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.FIRM,
                  "updating chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") with firmware "
                          + firmware.getVersion()));
          return firmware.getUrl();
        }
      }
    } else if (comparison < 0) {
      var firmwares = firmwareRepository
              .findAllByTypeAllowedDesc(typeAllowed);
      for (var firmware : firmwares) {
        if (firmware.getVersion().compareTo(firmwareVersion) < 0) {
          logger.info(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.FIRM,
                  "downgrading chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") with firmware "
                          + firmware.getVersion()));
          return firmware.getUrl();
        }
      }
    } else {
      logger.info(new BusinessLog(null,
              currentChargepoint,
              BusinessLogEntity.Category.FIRM,
              "chargepoint ("
                      + currentChargepoint.getSerialNumberChargepoint()
                      + ") is already updated"));
    }
    return "";
  }

  /**
   * Processes the received {@link ResetResponse16}.
   */
  private void processResetResponse() {
    if (chargePointManager.getCurrentChargepoint() != null) {
      var currentChargepoint = chargePointManager.getCurrentChargepoint();
      currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.FINISHED);
      currentChargepoint.setState(false);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
    }
  }

  /**
   * Processes the current firmware installation status.<br>
   * If the download has failed or the installation has failed,
   * we stop the process.<br>
   * Otherwise, we continue as normal.
   *
   * @param f {@link FirmwareStatusNotificationRequest16}.
   */
  private void processFirmwareStatusResponse(FirmwareStatusNotificationRequest16 f) {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    switch (f.status()) {
      case Installed -> {
        logger.info(new BusinessLog(null,
                currentChargepoint,
                BusinessLogEntity.Category.FIRM,
                "chargepoint ("
                        + currentChargepoint.getSerialNumberChargepoint()
                        + ") has successfully installed the firmware"));
        currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PENDING);
        chargepointRepository.save(currentChargepoint);
        var reset = new ResetRequest16(ResetType.Hard);
        sender.sendMessage(reset, chargePointManager);
        logger.info(new BusinessLog(null,
                currentChargepoint,
                BusinessLogEntity.Category.FIRM,
                "rebooting the chargepoint ("
                        + currentChargepoint.getSerialNumberChargepoint()
                        + ")"));
      }
      case DownloadFailed,
              InstallationFailed -> {
        if (f.status() == FirmwareStatus.DownloadFailed) {
          logger.warn(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.FIRM,
                  "firmware for the chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") couldn't be downloaded, "
                          + "check the internet connection of the chargepoint, SKIPPING"));
        } else {
          logger.warn(new BusinessLog(null,
                  currentChargepoint,
                  BusinessLogEntity.Category.FIRM,
                  "firmware for the chargepoint ("
                          + currentChargepoint.getSerialNumberChargepoint()
                          + ") couldn't be installed, check the given URL to the chargepoint, "
                          + "SKIPPING"));
        }
        //status.setStatus(Status.StatusProcess.FAILED);
        //status.setError(f.status());
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          // ignore
        }
        currentChargepoint.setStatusProcess(Chargepoint.StatusProcess.PENDING);
        currentChargepoint.setStep(Chargepoint.Step.CONFIGURATION);
        chargePointManager.notifyStatusUpdate();
        chargepointRepository.save(currentChargepoint);
        var reset = new ResetRequest16(ResetType.Hard);
        sender.sendMessage(reset, chargePointManager);
      }
      default -> {
        // Ignore, the chargepoint is downloading/installing.
      }
    }
  }
}
