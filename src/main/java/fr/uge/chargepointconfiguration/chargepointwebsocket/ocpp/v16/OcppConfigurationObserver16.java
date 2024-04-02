/*
 * The MIT License
 * Copyright © 2024 LastProject-ESIEE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.Reset.Type.HARD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ChargePointManager;
import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppMessageSender;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppObserver;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.BootNotificationResponse.BootNotificationResponseBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfiguration.ChangeConfigurationBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.Reset.ResetBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.UpdateFirmware.UpdateFirmwareBuilder;
import fr.uge.chargepointconfiguration.configuration.ConfigurationTranscriptor;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLog;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLog;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import fr.uge.chargepointconfiguration.typeallowed.TypeAllowed;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import org.springframework.lang.Nullable;

/**
 * Defines the OCPP 1.6 observer.<br>
 * An observer is defined by the interface {@link OcppObserver}.
 */
public class OcppConfigurationObserver16 implements OcppObserver {
  private final OcppMessageSender sender;
  private final ChargePointManager chargePointManager;
  private final ChargepointRepository chargepointRepository;
  private final FirmwareRepository firmwareRepository;
  private final Queue<ChangeConfiguration> queue = new LinkedList<>();
  private final CustomLogger logger;
  private String firmwareVersion;
  private String targetFirmwareVersion;
  private boolean loaded = false;
  private boolean lastOrderModeOn = false;

  /**
   * {@link OcppConfigurationObserver16}'s constructor.
   *
   * @param sender                {@link OcppMessageSender}.
   * @param chargePointManager    {@link ChargePointManager}.
   * @param chargepointRepository {@link ChargepointRepository}.
   * @param firmwareRepository    {@link FirmwareRepository}.
   * @param logger                {@link CustomLogger}.
   */
  public OcppConfigurationObserver16(
      OcppMessageSender sender,
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
  public Optional<OcppMessage> onMessage(OcppMessage ocppMessage) throws IOException {
    if (ocppMessage == null) {
      return processDefaultMessage();
    }
    return switch (ocppMessage) {
      case BootNotification b -> processBootNotification(b);
      case ChangeConfigurationResponse c -> processConfigurationResponse(c);
      case ResetResponse ignored -> processResetResponse();
      case FirmwareStatusNotification f -> processFirmwareStatusResponse(f);
      default -> processDefaultMessage();
    };
  }

  @Override
  public void onConnection(ChargePointManager chargePointManager) {
    Objects.requireNonNull(chargePointManager);
    // TODO : Is this method really useful ?
  }

  @Override
  public void onDisconnection(ChargePointManager chargePointManager) throws IOException {
    if (chargePointManager.getCurrentChargepoint() == null) {
      lastOrderModeOn = false;
      loaded = false;
      var reset = new ResetBuilder().withType(HARD).build();
      sender.sendMessage(reset, chargePointManager);
    }
  }

  /**
   * Processes the received {@link BootNotification} sent by the chargepoint.<br>
   * It is here where we start the automation of the configuration/firmware update.
   *
   * @param bootNotificationRequest16 {@link BootNotification}.
   */
  private Optional<OcppMessage> processBootNotification(BootNotification bootNotificationRequest16)
      throws IOException {
    firmwareVersion = bootNotificationRequest16.getFirmwareVersion();
    // Get charge point from database
    chargePointManager.setCurrentChargepoint(
        chargepointRepository.findBySerialNumberChargepointAndConstructor(
            bootNotificationRequest16.getChargePointSerialNumber(),
            bootNotificationRequest16.getChargePointVendor()));
    // If charge point is not found then skip it
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (currentChargepoint == null) {
      logger.warn(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND,
          "unknown chargepoint, serial number : "
              + bootNotificationRequest16.getChargePointSerialNumber()));
      var response = new BootNotificationResponseBuilder()
          .withCurrentTime(Instant.now())
          .withInterval(10)
          .withStatus(BootNotificationResponse.Status.REJECTED)
          .build();
      sender.sendMessage(response, chargePointManager);
      return Optional.of(response);
    }
    var config = currentChargepoint.getConfiguration();
    if (currentChargepoint.getConfiguration() == null) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
      chargepointRepository.save(currentChargepoint);
      chargePointManager.notifyStatusUpdate();
      logger.info(new BusinessLog(
          null,
          currentChargepoint,
          BusinessLogEntity.Category.LOGIN,
          "chargepoint ("
              + currentChargepoint.getSerialNumberChargePoint()
              + ") is authenticated"));
      var response = new BootNotificationResponseBuilder()
          .withCurrentTime(Instant.now())
          .withInterval(10)
          .withStatus(BootNotificationResponse.Status.ACCEPTED)
          .build();
      sender.sendMessage(response, chargePointManager);
      return Optional.of(response);
    }
    targetFirmwareVersion = config.getFirmware().getVersion();
    currentChargepoint.setState(true);
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
    logger.info(new BusinessLog(
        null,
        currentChargepoint,
        BusinessLogEntity.Category.LOGIN,
        "chargepoint (" + currentChargepoint.getSerialNumberChargePoint() + ") is authenticated"));
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    chargePointManager.notifyOnConnection();
    // Send BootNotification Response
    var response = new BootNotificationResponseBuilder()
        .withCurrentTime(Instant.now())
        .withInterval(5)
        .withStatus(BootNotificationResponse.Status.ACCEPTED)
        .build();
    sender.sendMessage(response, chargePointManager);
    return switch (currentChargepoint.getStep()) {
      case Chargepoint.Step.CONFIGURATION -> processConfigurationRequest();
      case Chargepoint.Step.FIRMWARE -> processFirmwareRequest();
    };
  }

  /**
   * Processes the configuration.<br>
   * It searches in database the configuration for the chargepoint,
   * loads the key-value and sends a {@link ChangeConfiguration} for the chargepoint.<br>
   */
  private Optional<OcppMessage> processConfigurationRequest() throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (queue.isEmpty() && !loaded) {
      loadKeyValue();
      loaded = true;
    }
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PROCESSING);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    var config = queue.poll();
    if (config == null && (loaded && !lastOrderModeOn)) {
      prepareLastOrder();
      lastOrderModeOn = true;
      return processConfigurationRequest();
    } else if (config == null && lastOrderModeOn) {
      logger.info(new BusinessLog(
          null,
          currentChargepoint,
          BusinessLogEntity.Category.CONFIG,
          "configuration for the chargepoint ("
              + currentChargepoint.getSerialNumberChargePoint()
              + ") is done ! "));
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      chargePointManager.notifyProcess();
      return processResetRequest();
    } else {
      sender.sendMessage(config, chargePointManager);
      assert config != null;
      return Optional.of(config);
    }
  }

  /**
   * Processes the {@link ChangeConfigurationResponse} sent by the chargepoint.<br>
   * If the change has been rejected, we stop the update.<br>
   * Otherwise, we continue.
   *
   * @param response {@link ChangeConfigurationResponse}.
   */
  private Optional<OcppMessage> processConfigurationResponse(ChangeConfigurationResponse response)
      throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    return switch (response.getStatus()) {
      case ACCEPTED, REBOOT_REQUIRED -> processConfigurationRequest();
      default -> {
        logger.warn(new BusinessLog(
            null,
            currentChargepoint,
            BusinessLogEntity.Category.CONFIG,
            "configuration for the chargepoint ("
                + currentChargepoint.getSerialNumberChargePoint()
                + ") has failed, see its status ! "));
        currentChargepoint.setStatus(Chargepoint.StatusProcess.FAILED);
        currentChargepoint.setError(response.getStatus().name());
        chargepointRepository.save(currentChargepoint);
        // Dispatch information to users
        chargePointManager.notifyStatusUpdate();
        chargePointManager.notifyProcess();
        queue.clear();
        yield Optional.empty();
      }
    };
  }

  private void prepareLastOrder() {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    logger.info(new BusinessLog(
        null,
        currentChargepoint,
        BusinessLogEntity.Category.CONFIG,
        "configuration for the chargepoint ("
            + currentChargepoint.getSerialNumberChargePoint()
            + ") is almost done, sending last order ! "));
    var clientId = currentChargepoint.getClientId();
    var changeConfig = new ChangeConfigurationBuilder()
        .withKey(ConfigurationTranscriptor.CHARGEPOINT_IDENTITY
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(firmwareVersion))
        .withValue(clientId)
        .build();
    queue.add(changeConfig);
    var finalServerAddress = System.getenv("FINAL_WS_SERVER_ADDRESS");
    if (finalServerAddress != null) {
      changeConfig = new ChangeConfigurationBuilder()
          .withKey(ConfigurationTranscriptor.NETWORK_PROFILE
              .getOcpp16Key()
              .getFirmwareKeyAccordingToVersion(firmwareVersion))
          .withValue(ConfigurationTranscriptor.NETWORK_PROFILE
              .getOcpp16Key()
              .getValueFormatAccordingToVersion(firmwareVersion)
              .formatted(finalServerAddress))
          .build();
      queue.add(changeConfig);
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
  private Optional<OcppMessage> processFirmwareRequest() throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (currentChargepoint.getConfiguration() == null) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
      chargepointRepository.save(currentChargepoint);
      chargePointManager.notifyStatusUpdate();
      return Optional.empty();
    }
    var firmware = currentChargepoint.getConfiguration().getFirmware();
    logger.info(new BusinessLog(
        null,
        currentChargepoint,
        BusinessLogEntity.Category.FIRM,
        "firmware target ("
            + firmware.getVersion()
            + ") for chargepoint ( "
            + currentChargepoint.getSerialNumberChargePoint()
            + ")"));
    var typesAllowed = firmware.getTypesAllowed();
    URI link = null;
    for (var typeAllowed : typesAllowed) {
      if (typeAllowed.getType().equals(currentChargepoint.getType())
          && typeAllowed.getConstructor().equals(currentChargepoint.getConstructor())) {
        link = fetchUrlFromFirstCompatibleVersion(typeAllowed);
        break;
      }
    }
    if (link == null) {
      logger.info(new BusinessLog(
          null,
          currentChargepoint,
          BusinessLogEntity.Category.FIRM,
          "firmware update for the chargepoint ("
              + currentChargepoint.getSerialNumberChargePoint()
              + ") is done ! "));
      currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
      currentChargepoint.setStep(Chargepoint.Step.CONFIGURATION);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      chargePointManager.notifyProcess();
      return processConfigurationRequest();
    }
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PROCESSING);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    var firmwareRequest = new UpdateFirmwareBuilder()
        .withLocation(link)
        .withRetrieveDate(Instant.now())
        .build();
    sender.sendMessage(firmwareRequest, chargePointManager);
    return Optional.of(firmwareRequest);
  }

  /**
   * Fetches the first compatible firmware version from the database.
   *
   * @param typeAllowed {@link TypeAllowed}.
   * @return The URL for downloading the firmware.
   */
  private @Nullable URI fetchUrlFromFirstCompatibleVersion(TypeAllowed typeAllowed) {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    var comparison = targetFirmwareVersion.compareTo(firmwareVersion);
    if (comparison > 0) {
      var firmwares = firmwareRepository.findAllByTypeAllowedAsc(typeAllowed);
      for (var firmware : firmwares) {
        if (firmware.getVersion().compareTo(firmwareVersion) > 0) {
          logger.info(new BusinessLog(
              null,
              currentChargepoint,
              BusinessLogEntity.Category.FIRM,
              "updating chargepoint ("
                  + currentChargepoint.getSerialNumberChargePoint()
                  + ") with firmware "
                  + firmware.getVersion()));
          try {
            return new URI(firmware.getUrl());
          } catch (URISyntaxException e) {
            throw new IllegalStateException("Firmware URL is invalid: " + firmware.getUrl(), e);
          }
        }
      }
      logger.info(new BusinessLog(
          null,
          currentChargepoint,
          BusinessLogEntity.Category.FIRM,
          "didn't find any compatible firmware for chargepoint ("
              + currentChargepoint.getSerialNumberChargePoint()
              + ") : skipping to CONFIGURATION"));
      return null;
    } else if (comparison < 0) {
      var firmwares = firmwareRepository.findAllByTypeAllowedDesc(typeAllowed);
      for (var firmware : firmwares) {
        if (firmware.getVersion().compareTo(firmwareVersion) < 0) {
          logger.info(new BusinessLog(
              null,
              currentChargepoint,
              BusinessLogEntity.Category.FIRM,
              "tried to rollback chargepoint ("
                  + currentChargepoint.getSerialNumberChargePoint()
                  + ") with firmware : FORBIDDEN, skipping to CONFIGURATION"));
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Processes the received {@link ResetResponse}.
   */
  private Optional<OcppMessage> processResetResponse() {
    if (chargePointManager.getCurrentChargepoint() != null) {
      var currentChargepoint = chargePointManager.getCurrentChargepoint();
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
      currentChargepoint.setState(false);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
    }
    return Optional.empty();
  }

  private Optional<OcppMessage> processResetRequest() throws IOException {
    var reset = new ResetBuilder().withType(HARD).build();
    sender.sendMessage(reset, chargePointManager);
    return Optional.of(reset);
  }

  /**
   * Processes the current firmware installation status.<br>
   * If the download has failed or the installation has failed,
   * we stop the process.<br>
   * Otherwise, we continue as normal.
   *
   * @param f {@link FirmwareStatusNotification}.
   */
  private Optional<OcppMessage> processFirmwareStatusResponse(FirmwareStatusNotification f)
      throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    return switch (f.getStatus()) {
      case INSTALLED -> {
        logger.info(new BusinessLog(
            null,
            currentChargepoint,
            BusinessLogEntity.Category.FIRM,
            "chargepoint ("
                + currentChargepoint.getSerialNumberChargePoint()
                + ") has successfully installed the firmware"));
        currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
        chargepointRepository.save(currentChargepoint);
        var reset = new ResetBuilder().withType(HARD).build();
        sender.sendMessage(reset, chargePointManager);
        logger.info(new BusinessLog(
            null,
            currentChargepoint,
            BusinessLogEntity.Category.FIRM,
            "rebooting the chargepoint (" + currentChargepoint.getSerialNumberChargePoint() + ")"));
        yield Optional.of(reset);
      }
      case DOWNLOAD_FAILED, INSTALLATION_FAILED -> {
        var message = "firmware for the chargepoint ("
            + currentChargepoint.getSerialNumberChargePoint()
            + ") couldn't be installed, ";
        if (f.getStatus() == FirmwareStatusNotification.Status.DOWNLOAD_FAILED) {
          message += "check the internet connection of the chargepoint";
        } else {
          message += "check the given URL to the chargepoint";
        }
        logger.warn(
            new BusinessLog(null, currentChargepoint, BusinessLogEntity.Category.FIRM, message));
        currentChargepoint.setStatus(Chargepoint.StatusProcess.FAILED);
        chargepointRepository.save(currentChargepoint);
        chargePointManager.notifyStatusUpdate();
        chargePointManager.notifyProcess();
        yield Optional.empty();
      }
      default -> Optional.empty();
    };
  }

  private void loadKeyValue() {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    // The change configuration list is empty, so we load the configuration
    var configuration = currentChargepoint.getConfiguration().getConfiguration();
    var mapper = new ObjectMapper();
    try {
      // Cast
      HashMap<String, String> configMap = mapper.readValue(configuration, HashMap.class);
      configMap.forEach((key, value) -> {
        var configMessage = new ChangeConfigurationBuilder()
            .withKey(ConfigurationTranscriptor.idToEnum(Integer.parseInt(key))
                .getOcpp16Key()
                .getFirmwareKeyAccordingToVersion(firmwareVersion))
            .withValue(value)
            .build();
        queue.add(configMessage);
        logger.info(new BusinessLog(
            null,
            currentChargepoint,
            BusinessLogEntity.Category.CONFIG,
            "added configuration in the waiting list for the chargepoint ("
                + currentChargepoint.getSerialNumberChargePoint()
                + ") : "
                + configMessage));
      });
    } catch (JsonProcessingException e) {
      logger.error(new BusinessLog(
          null,
          currentChargepoint,
          BusinessLogEntity.Category.CONFIG,
          "couldn't read configuration for the chargepoint ("
              + currentChargepoint.getSerialNumberChargePoint()
              + "): " + e.getMessage()));
    }
  }

  private Optional<OcppMessage> processDefaultMessage() throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (currentChargepoint == null) {
      return processResetRequest();
    }
    chargePointManager.setCurrentChargepoint(
        chargepointRepository.findBySerialNumberChargepointAndConstructor(
            currentChargepoint.getSerialNumberChargePoint(), currentChargepoint.getConstructor()));
    currentChargepoint = chargePointManager.getCurrentChargepoint();
    var step = currentChargepoint.getStep();
    var status = currentChargepoint.getStatus();
    if (step == Chargepoint.Step.CONFIGURATION && status == Chargepoint.StatusProcess.PENDING) {
      return processConfigurationRequest();
    } else if (status == Chargepoint.StatusProcess.FAILED
        || status == Chargepoint.StatusProcess.FINISHED) {
      loaded = false;
      lastOrderModeOn = false;
      return processFirmwareRequest();
    } else {
      return Optional.empty();
    }
  }
}
