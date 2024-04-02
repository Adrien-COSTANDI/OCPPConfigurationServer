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
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp;

import fr.uge.chargepointconfiguration.chargepointwebsocket.WebSocketMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.OcppMessageParser16;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v201.OcppMessageParser201;
import java.util.Objects;
import java.util.Optional;

/**
 * Interface used to define an OCPP message parser.<br>
 * TODO : We could use an Observer to avoid redundancy.
 */
public interface OcppMessageParser {

  /**
   * Parses the request web socket message sent by the chargepoint
   * into an OCPP message.<br>
   * Returns an empty optional if the packet is unknown.
   *
   * @param webSocketMessage {@link WebSocketMessage}.
   * @return An optional of an {@link OcppMessage}.
   */
  Optional<OcppMessage> parseRequestMessage(WebSocketMessage webSocketMessage);

  /**
   * Parses the request web socket message sent by the server
   * into an OCPP message.<br>
   * This method should be called after the server sent a request to the chargepoint.<br>
   * Returns an empty optional if the packet is unknown.
   *
   * @param requestMessage The {@link WebSocketMessage} the server sent to the chargepoint.
   * @param responseMessage The {@link WebSocketMessage} response sent by the chargepoint.
   * @return An optional of an {@link OcppMessage}.
   */
  Optional<OcppMessage> parseResponseMessage(
      WebSocketMessage requestMessage, WebSocketMessage responseMessage);

  /**
   * Parses the OCPP message into a String.
   *
   * @param message {@link OcppMessage}.
   * @return The OCPP message into a formatted String.
   */
  String transform(OcppMessage message);

  /**
   * Instantiate the correct parser according to the OCPP version.
   *
   * @param ocppVersion {@link OcppVersion}.
   * @return {@link OcppMessageParser}.
   */
  static OcppMessageParser instantiateFromVersion(OcppVersion ocppVersion) {
    Objects.requireNonNull(ocppVersion);
    return switch (ocppVersion) {
      case V1_6 -> new OcppMessageParser16();
      case V2_0_1 -> new OcppMessageParser201();
    };
  }
}
