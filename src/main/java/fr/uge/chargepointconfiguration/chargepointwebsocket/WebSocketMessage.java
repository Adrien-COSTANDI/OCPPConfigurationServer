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
package fr.uge.chargepointconfiguration.chargepointwebsocket;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.MessageType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.BootNotification;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfiguration;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.FirmwareStatusNotification;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.Reset;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.UpdateFirmware;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v201.BootNotificationRequest;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v201.SetVariablesRequest;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the message sent or received via a web socket.
 */
public interface WebSocketMessage {

  /**
   * Returns the message's data.<br>
   * Often, it is information concerning a change in the configuration
   * or information about the chargepoint.<br>
   * It is represented by a json formatted string.
   *
   * @return The message's data (json formatted string).
   */
  String data();

  /**
   * Returns the message's type.<br>
   * By default, it returns null when the message is not a request.
   *
   * @return {@link MessageTypeRequest}.
   */
  default MessageTypeRequest messageName() {
    return null;
  }

  /**
   * Returns the message's id.<br>
   * It is used to respond to the message or wait for the correct message.
   *
   * @return The message's id.
   */
  long messageId();

  /**
   * Checks if the current message is a request or not.<br>
   * For the false value, the current message could be a Response or Other.
   *
   * @return True if the message is a request or false, if not.
   */
  boolean isRequest();

  /**
   * Defines the request message type for OCPP 1.6 and OCPP 2.0.1 protocols.<br>
   * These enums can be sent by the chargepoint (BootNotification, StatusFirmware)
   * or the server (ChangeConfiguration, SetVariables).<br>
   * By default, if the packet is unknown, we set to other.
   */
  enum MessageTypeRequest {
    BOOT_NOTIFICATION_REQUEST("BootNotification"),
    STATUS_FIRMWARE_REQUEST("FirmwareStatusNotification"),
    UPDATE_FIRMWARE_REQUEST("UpdateFirmware"),
    CHANGE_CONFIGURATION_REQUEST("ChangeConfiguration"),
    SET_VARIABLES_REQUEST("SetVariables"),
    RESET_REQUEST("Reset"),
    OTHER("Other");

    private final String name;

    /**
     * MessageTypeReceived's constructor.
     *
     * @param name The correct packet name.
     */
    MessageTypeRequest(String name) {
      this.name = Objects.requireNonNull(name);
    }

    /**
     * Returns the corresponding packet's name.
     *
     * @return The packet's name.
     */
    public String getName() {
      return name;
    }

    /**
     * Converts the OCPP message type into the enum.
     *
     * @param ocppMessage The OCPP message received by the server.
     * @return The correct enum if found, an OTHER by default.
     */
    public static MessageTypeRequest ocppMessageToEnum(OcppMessage ocppMessage) {
      Objects.requireNonNull(ocppMessage);
      return switch (ocppMessage) {
        case BootNotification ignored -> BOOT_NOTIFICATION_REQUEST;
        case BootNotificationRequest ignored -> BOOT_NOTIFICATION_REQUEST;
        case ChangeConfiguration ignored -> CHANGE_CONFIGURATION_REQUEST;
        case UpdateFirmware ignored -> UPDATE_FIRMWARE_REQUEST;
        case SetVariablesRequest ignored -> SET_VARIABLES_REQUEST;
        case Reset ignored -> RESET_REQUEST;
        case FirmwareStatusNotification ignored -> STATUS_FIRMWARE_REQUEST;
        default -> OTHER;
      };
    }

    /**
     * Converts the message name into an enum to make the process
     * message easier.
     *
     * @param messageName The message type received from the remote.
     * @return The correct enum if found, an OTHER by default.
     */
    public static MessageTypeRequest nameToEnum(String messageName) {
      Objects.requireNonNull(messageName);
      return switch (messageName) {
        case "BootNotification" -> BOOT_NOTIFICATION_REQUEST;
        case "FirmwareStatusNotification" -> STATUS_FIRMWARE_REQUEST;
        case "UpdateFirmware" -> UPDATE_FIRMWARE_REQUEST;
        case "ChangeConfiguration" -> CHANGE_CONFIGURATION_REQUEST;
        case "Reset" -> RESET_REQUEST;
        default -> OTHER;
      };
    }
  }

  /**
   * Parse string message received from the web socket.
   *
   * @param message Received message from the web socket connection.
   * @return An optional of {@link WebSocketMessage}.
   */
  static Optional<WebSocketMessage> parse(String message) {
    Objects.requireNonNull(message);
    // Split this line by 4 by default for a request type message.
    var array = message.substring(1, message.length() - 1).split(",", 4);
    var callType = Integer.parseInt(array[0]);
    return switch (MessageType.codeToEnum(callType)) {
      case RESPONSE -> {
        try {
          // Split this line by 3, because a response has 3 parts (MessageType, MessageId, Data).
          array = message.substring(1, message.length() - 1).split(",", 3);
          var messageId = Long.parseLong(array[1].replaceAll("\"", ""));
          yield Optional.of(new WebSocketResponseMessage(callType, messageId, array[2]));
        } catch (NumberFormatException n) {
          yield Optional.empty();
        }
      }
      case REQUEST -> {
        try {
          var messageId = Long.parseLong(array[1].replaceAll("\"", ""));
          var messageName =
              MessageTypeRequest.nameToEnum(array[2].substring(1, array[2].length() - 1));
          yield Optional.of(
              new WebSocketRequestMessage(callType, messageId, messageName, array[3]));
        } catch (NumberFormatException n) {
          yield Optional.empty();
        }
      }
      case UNKNOWN -> // TODO : Log, unknown type.
      Optional.empty();
    };
  }
}
