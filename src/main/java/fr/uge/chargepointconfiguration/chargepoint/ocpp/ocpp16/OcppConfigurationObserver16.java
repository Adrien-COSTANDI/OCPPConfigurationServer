package fr.uge.chargepointconfiguration.chargepoint.ocpp.ocpp16;

import fr.uge.chargepointconfiguration.WebSocketHandler;
import fr.uge.chargepointconfiguration.chargepoint.ChargePointManager;
import fr.uge.chargepointconfiguration.chargepoint.OcppMessageSender;
import fr.uge.chargepointconfiguration.chargepoint.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepoint.ocpp.OcppObserver;
import fr.uge.chargepointconfiguration.chargepoint.ocpp.RegistrationStatus;
import fr.uge.chargepointconfiguration.repository.ChargepointRepository;
import fr.uge.chargepointconfiguration.repository.FirmwareRepository;
import fr.uge.chargepointconfiguration.repository.StatusRepository;
import java.time.LocalDateTime;

/**
 * Defines the OCPP configuration message for the visitor.
 */
public class OcppConfigurationObserver16 implements OcppObserver {
  private final OcppMessageSender sender;
  private final ChargepointRepository chargepointRepository;
  private final FirmwareRepository firmwareRepository;
  private final StatusRepository statusRepository;
  private boolean authenticated;

  /**
   * Constructor for the OCPP 1.6 configuration observer.
   *
   * @param sender websocket channel to send message
   * @param chargepointRepository charge point repository
   * @param firmwareRepository firmware repository
   * @param statusRepository charge point status repository
   */
  public OcppConfigurationObserver16(OcppMessageSender sender,
                                     ChargepointRepository chargepointRepository,
                                     FirmwareRepository firmwareRepository,
                                     StatusRepository statusRepository) {
    this.sender = sender;
    this.chargepointRepository = chargepointRepository;
    this.firmwareRepository = firmwareRepository;
    this.statusRepository = statusRepository;
  }

  @Override
  public void onMessage(OcppMessage ocppMessage,
                        ChargePointManager chargePointManager, long messageId) {
    switch (ocppMessage) {
      case BootNotificationRequest16 b -> processBootNotification(b, messageId, chargePointManager);
      default -> {
        // Do nothing
      }
    }
  }

  @Override
  public void onConnection(ChargePointManager chargePointManager) {

  }

  @Override
  public void onDisconnection(ChargePointManager chargePointManager) {

  }

  private void processBootNotification(
          BootNotificationRequest16 bootNotificationRequest16,
          long messageId, ChargePointManager chargePointManager) {
    // Get charge point from database
    var chargePoint = chargepointRepository.findBySerialNumberChargepointAndConstructor(
            bootNotificationRequest16.chargePointSerialNumber(),
            bootNotificationRequest16.chargePointVendor()
    );
    // If charge point is not found then skip it
    if (chargePoint == null) {
      //TODO: add log
      return;
    }
    authenticated = true;
    // Dispatch information to users
    WebSocketHandler.sendMessageToUsers("Charge point x connected !");
    // Send BootNotification Response
    var response = new BootNotificationResponse16(
            LocalDateTime.now().toString(),
            5,
            RegistrationStatus.Accepted
    );
    sender.sendMessage(response, messageId);
  }
}