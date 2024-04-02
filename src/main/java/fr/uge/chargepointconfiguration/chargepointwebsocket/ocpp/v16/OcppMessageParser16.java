package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import fr.uge.chargepointconfiguration.chargepointwebsocket.WebSocketMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessageParser;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import java.util.Objects;
import java.util.Optional;

/**
 * Parses the OCPP 1.6 messages.
 */
public class OcppMessageParser16 implements OcppMessageParser {

  @Override
  public Optional<OcppMessage> parseRequestMessage(WebSocketMessage webSocketMessage) {
    Objects.requireNonNull(webSocketMessage);
    return switch (webSocketMessage.messageName()) {
      case BOOT_NOTIFICATION_REQUEST -> Optional.of(
          JsonParser.stringToObject(BootNotification.class, webSocketMessage.data()));
      case STATUS_FIRMWARE_REQUEST -> Optional.of(
          JsonParser.stringToObject(FirmwareStatusNotification.class, webSocketMessage.data()));
      default -> Optional.empty();
    };
  }

  @Override
  public Optional<OcppMessage> parseResponseMessage(
      WebSocketMessage requestMessage, WebSocketMessage responseMessage) {
    Objects.requireNonNull(requestMessage);
    Objects.requireNonNull(responseMessage);
    return switch (requestMessage.messageName()) {
      case CHANGE_CONFIGURATION_REQUEST -> Optional.of(
          JsonParser.stringToObject(ChangeConfigurationResponse.class, responseMessage.data()));
      case RESET_REQUEST -> Optional.of(
          JsonParser.stringToObject(ResetResponse.class, responseMessage.data()));
      case UPDATE_FIRMWARE_REQUEST -> Optional.of(JsonParser.stringToObject(
          UpdateFirmwareResponse.class, "{}")); // Empty value because it is an acknowledgement
      default -> Optional.empty();
    };
  }

  @Override
  public String transform(OcppMessage message) {
    Objects.requireNonNull(message);
    return JsonParser.objectToJsonString(message);
  }
}
