package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp2;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.RegistrationStatusEnum.ACCEPTED;
import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.RegistrationStatusEnum.REJECTED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ChargePointManager;
import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppMessageSender;
import fr.uge.chargepointconfiguration.chargepointwebsocket.WebSocketMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.WebSocketRequestMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.MessageType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppObserver;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.BootNotificationRequest;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.BootNotificationResponse.BootNotificationResponseBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.Component.ComponentBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.SetVariableData;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.SetVariableData.SetVariableDataBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.SetVariablesRequest.SetVariablesRequestBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.SetVariablesResponse;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.Variable.VariableBuilder;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Defines the OCPP configuration message for the visitor.
 */
public class OcppConfigurationObserver20 implements OcppObserver {
  private final OcppMessageSender sender;
  private final ChargePointManager chargePointManager;
  private final ChargepointRepository chargepointRepository;
  private final Queue<SetVariableData> queue = new LinkedList<>();

  /**
   * Constructor for the OCPP 2.0 configuration observer.
   *
   * @param sender                websocket channel to send message
   * @param chargepointRepository charge point repository
   */
  public OcppConfigurationObserver20(
      OcppMessageSender sender,
      ChargePointManager chargePointManager,
      ChargepointRepository chargepointRepository) {
    this.sender = sender;
    this.chargePointManager = chargePointManager;
    this.chargepointRepository = chargepointRepository;
  }

  @Override
  public Optional<OcppMessage> onMessage(OcppMessage ocppMessage) throws IOException {
    switch (ocppMessage) {
      case BootNotificationRequest b -> processBootNotification(b);
      case SetVariablesResponse r -> processConfigurationResponse(r);
      default -> {
        // Do nothing
      }
    }
    return Optional.empty();
  }

  @Override
  public void onConnection(ChargePointManager chargePointManager) {}

  @Override
  public void onDisconnection(ChargePointManager chargePointManager) {}

  private void processBootNotification(BootNotificationRequest bootNotificationRequest)
      throws IOException {

    // Get charge point from database
    chargePointManager.setCurrentChargepoint(
        chargepointRepository.findBySerialNumberChargepointAndConstructor(
            bootNotificationRequest.getChargingStation().getSerialNumber(),
            bootNotificationRequest.getChargingStation().getVendorName()));
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    // If charge point is not found then skip it
    if (currentChargepoint == null) {
      var response = new BootNotificationResponseBuilder()
          .withCurrentTime(Instant.now())
          .withInterval(5)
          .withStatus(REJECTED)
          .build();
      sender.sendMessage(response, chargePointManager);
      return;
    }
    currentChargepoint.setState(true);
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    // Send BootNotification Response
    var response = new BootNotificationResponseBuilder()
        .withCurrentTime(Instant.now())
        .withInterval(5)
        .withStatus(ACCEPTED)
        .build();
    sender.sendMessage(response, chargePointManager);
    if (currentChargepoint.getConfiguration() == null) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
      chargepointRepository.save(currentChargepoint);
      chargePointManager.notifyStatusUpdate();
      return;
    }
    var config = new SetVariablesRequestBuilder()
        .withSetVariableData(List.of(new SetVariableDataBuilder()
            .withAttributeValue("")
            .withComponent(new ComponentBuilder().withName("none").build())
            .withVariable(new VariableBuilder().withName("LightIntensity").build())
            .build()))
        .build();
    sender.sendMessage(config, chargePointManager);
    switch (currentChargepoint.getStep()) {
      case Chargepoint.Step.CONFIGURATION -> processConfigurationRequest();
      case Chargepoint.Step.FIRMWARE -> processFirmwareRequest();
      default -> {
        // ignore
      }
    }
  }

  private void processConfigurationRequest() throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    var configuration = currentChargepoint.getConfiguration();
    if (configuration == null) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      return;
    }
    var mapper = new ObjectMapper();
    HashMap<String, HashMap<String, String>> configMap;
    try {
      configMap = mapper.readValue(configuration.getConfiguration(), HashMap.class);
    } catch (JsonProcessingException e) {
      return;
    }
    for (var component : configMap.keySet()) {
      var componentConfig = configMap.get(component);
      for (var key : componentConfig.keySet()) {
        var value = componentConfig.get(key);
        queue.add(new SetVariableDataBuilder()
            .withAttributeValue(value)
            .withComponent(new ComponentBuilder().withName(component).build())
            .withVariable(new VariableBuilder().withName(key).build())
            .build());
      }
    }
    if (queue.isEmpty()) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
    } else {
      currentChargepoint.setState(true);
      currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      var setVariableData = new ArrayList<
          fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_20.SetVariableData>();
      while (!queue.isEmpty()) {
        setVariableData.add(queue.poll());
      }
      var setVariableRequest =
          new SetVariablesRequestBuilder().withSetVariableData(setVariableData).build();
      sender.sendMessage(setVariableRequest, chargePointManager);
      chargePointManager.setPendingRequest(new WebSocketRequestMessage(
          MessageType.REQUEST.getCallType(),
          chargePointManager.getCurrentId(),
          WebSocketMessage.MessageTypeRequest.SET_VARIABLES_REQUEST,
          JsonParser.objectToJsonString(setVariableRequest)));
    }
  }

  private void processFirmwareRequest() throws IOException {
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PROCESSING);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    chargepointRepository.save(currentChargepoint);
    // TODO : Send a update firmware request !
    currentChargepoint.setStatus(Chargepoint.StatusProcess.PENDING);
    currentChargepoint.setStep(Chargepoint.Step.CONFIGURATION);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
    processConfigurationRequest();
  }

  private void processConfigurationResponse(SetVariablesResponse response) {
    var noFailure = true;
    var failedConfig = new StringBuilder();
    for (var result : response.getSetVariableResult()) {
      if (!result.getAttributeStatus().equals(ACCEPTED)) {
        noFailure = false;
        failedConfig
            .append(result.getAttributeStatus())
            .append(" :\n\tComponent : ")
            .append(result.getComponent().getName())
            .append("\n\tVariable : ")
            .append(result.getVariable().getName())
            .append("\n");
      }
    }
    var currentChargepoint = chargePointManager.getCurrentChargepoint();
    if (!noFailure) {
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FAILED);
      currentChargepoint.setError(failedConfig.toString());
      chargepointRepository.save(currentChargepoint);
      // Dispatch information to users
      chargePointManager.notifyStatusUpdate();
      return;
    }
    currentChargepoint.setStatus(Chargepoint.StatusProcess.FINISHED);
    chargepointRepository.save(currentChargepoint);
    // Dispatch information to users
    chargePointManager.notifyStatusUpdate();
  }
}
