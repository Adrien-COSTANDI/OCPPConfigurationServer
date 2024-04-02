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
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v201;

import fr.uge.chargepointconfiguration.chargepointwebsocket.WebSocketMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessageParser;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import java.util.Objects;
import java.util.Optional;

/**
 * Parses an OCPP 2.0.1 message.
 */
public class OcppMessageParser201 implements OcppMessageParser {

  private final JsonParser jsonParser;

  public OcppMessageParser201() {
    jsonParser = new JsonParser();
  }

  @Override
  public Optional<OcppMessage> parseRequestMessage(WebSocketMessage webSocketMessage) {
    Objects.requireNonNull(webSocketMessage);
    return switch (webSocketMessage.messageName()) {
      case BOOT_NOTIFICATION_REQUEST -> Optional.of(
          jsonParser.stringToObject(BootNotificationRequest.class, webSocketMessage.data()));
      default -> Optional.empty();
    };
  }

  @Override
  public Optional<OcppMessage> parseResponseMessage(
      WebSocketMessage requestMessage, WebSocketMessage responseMessage) {
    Objects.requireNonNull(requestMessage);
    Objects.requireNonNull(responseMessage);
    return switch (requestMessage.messageName()) {
      case SET_VARIABLES_REQUEST -> Optional.of(
          jsonParser.stringToObject(SetVariablesResponse.class, responseMessage.data()));
      default -> Optional.empty();
    };
  }

  @Override
  public String transform(OcppMessage message) {
    return jsonParser.objectToJsonString(message);
  }
}
