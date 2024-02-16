package fr.uge.chargepointconfiguration.chargepoint;

/**
 * A record to define a message received by the server from the remote.
 *
 * @param callType    The call's type given by the message.
 * @param messageId   The message's id which is useful if we need to answer.
 * @param messageName An enumeration to define the message type.
 * @param data        The data given by the message, it is in Json format.
 */
public record WebSocketRequestMessage(int callType,
                                      String messageId,
                                      WebSocketMessageName messageName,
                                      String data) {

  /**
   * Defines the message type sent by the remote.
   */
  public enum WebSocketMessageName {
    BOOT_NOTIFICATION_REQUEST,
    UPDATE_FIRMWARE_RESPONSE,
    STATUS_FIRMWARE_REQUEST,
    CHANGE_CONFIGURATION_RESPONSE,
    OTHER;

    /**
     * Converts the message name into an enum to make the process
     * message easier.
     *
     * @param messageName The message type received from the remote.
     * @return The correct enum if found, an IllegalArgumentException if not.
     */
    public static WebSocketMessageName nameToEnum(String messageName) {
      return switch (messageName) {
        case "BootNotification" -> BOOT_NOTIFICATION_REQUEST;
        case "UpdateFirmware" -> UPDATE_FIRMWARE_RESPONSE;
        case "StatusFirmware" -> STATUS_FIRMWARE_REQUEST;
        case "ChangeConfiguration" -> CHANGE_CONFIGURATION_RESPONSE;
        default -> OTHER;
      };
    }

  }

  /**
   * Parse string message received from the web socket.<br>
   * TODO : improve this method because it is not really safe, the split could be wrong !
   *
   * @param message - received message
   * @return WebSocketMessage
   */
  public static WebSocketRequestMessage parse(String message) {
    var array = message.substring(1, message.length() - 1).split(",", 4);
    int callType = Integer.parseInt(array[0]);
    String messageId = array[1];
    var messageName = WebSocketMessageName.nameToEnum(array[2].substring(1, array[2].length() - 1));
    return new WebSocketRequestMessage(callType, messageId, messageName, array[3]);
  }

  @Override
  public String toString() {
    return "[" + callType + ",\"" + messageId + "\",\"" + messageName + "\"," + data + "]";
  }
}