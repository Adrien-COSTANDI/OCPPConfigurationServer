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

import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.MessageType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppVersion;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLog;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import jakarta.validation.Validator;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Objects;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class OcppWebSocketHandler extends TextWebSocketHandler {
  private final HashMap<InetSocketAddress, ChargePointManager> chargePoints = new HashMap<>();
  private final ChargepointRepository chargepointRepository;
  private final FirmwareRepository firmwareRepository;
  private final Validator validator;
  private final CustomLogger logger;

  public OcppWebSocketHandler(
      ChargepointRepository chargepointRepository,
      FirmwareRepository firmwareRepository,
      Validator validator,
      CustomLogger logger) {
    this.chargepointRepository = Objects.requireNonNull(chargepointRepository);
    this.firmwareRepository = Objects.requireNonNull(firmwareRepository);
    this.validator = validator;
    this.logger = Objects.requireNonNull(logger);
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.info(new TechnicalLog(
        TechnicalLogEntity.Component.BACKEND, "new connection to " + session.getRemoteAddress()));
    var ocppVersion =
        OcppVersion.parse(session.getHandshakeHeaders().getFirst("Sec-Websocket-Protocol"));
    if (ocppVersion.isPresent()) {
      chargePoints.putIfAbsent(
          session.getRemoteAddress(), instantiate(ocppVersion.orElseThrow(), session));
    } else {
      logger.info(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, "Unknown OCPP version !"));
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    super.handleTextMessage(session, message);
    var remote = session.getRemoteAddress();
    var webSocketMessage = WebSocketMessage.parse(message.getPayload());
    if (webSocketMessage.isEmpty()) {
      logger.warn(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND, "failed to parse message from " + remote));
      return;
    }
    if (webSocketMessage.get().isRequest()) {
      logger.info(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND,
          "received request from " + remote + ": " + message.getPayload()));
    } else {
      logger.info(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND,
          "received response from " + remote + ": " + message.getPayload()));
    }
    var violations = validator.validate(webSocketMessage.get());
    if (!violations.isEmpty()) {
      logger.warn(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND,
          "message from " + remote + " is invalid: " + violations));
      return;
    }
    chargePoints.get(remote).processMessage(webSocketMessage.get());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    super.handleTransportError(session, exception);
    if (exception instanceof EOFException) {
      logger.warn(new TechnicalLog(
          TechnicalLogEntity.Component.BACKEND,
          "connection closed by " + session.getRemoteAddress()));
      return;
    }
    logger.error(new TechnicalLog(
        TechnicalLogEntity.Component.BACKEND,
        "an error occurred on connection " + session.getRemoteAddress() + " : " + exception));
    var chargepoint = chargePoints.get(session.getRemoteAddress());
    if (chargepoint != null) {
      chargepoint.onError(exception);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
    logger.warn(new TechnicalLog(
        TechnicalLogEntity.Component.BACKEND,
        "closed "
            + session.getRemoteAddress()
            + " with exit code "
            + session
            + " additional info: "
            + status.getReason()));
    var chargepoint = chargePoints.getOrDefault(session.getRemoteAddress(), null);
    if (chargepoint != null) {
      chargepoint.onDisconnection();
    }
    chargePoints.remove(session.getRemoteAddress());
    session.close();
  }

  private ChargePointManager instantiate(OcppVersion ocppVersion, WebSocketSession session) {
    return new ChargePointManager(
        ocppVersion,
        (ocppMessage, chargePointManager) -> {
          var violations = validator.validate(ocppMessage);
          if (!violations.isEmpty()) {
            logger.warn(new TechnicalLog(
                TechnicalLogEntity.Component.BACKEND, "message is invalid: " + violations));
            return;
          }
          switch (OcppMessage.ocppMessageToMessageType(ocppMessage)) {
            case REQUEST -> {
              var request = new WebSocketRequestMessage(
                  MessageType.REQUEST.getCallType(),
                  chargePointManager.getCurrentId(),
                  WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(ocppMessage),
                  JsonParser.objectToJsonString(ocppMessage));
              chargePointManager.setPendingRequest(request);
              session.sendMessage(new TextMessage(request.toString()));
              logger.info(new TechnicalLog(
                  TechnicalLogEntity.Component.BACKEND,
                  "sent request to " + session.getRemoteAddress() + " : " + request));
            }
            case RESPONSE -> {
              var response = new WebSocketResponseMessage(
                  MessageType.RESPONSE.getCallType(),
                  chargePointManager.getCurrentId(),
                  JsonParser.objectToJsonString(ocppMessage));
              session.sendMessage(new TextMessage(response.toString()));
              logger.info(new TechnicalLog(
                  TechnicalLogEntity.Component.BACKEND,
                  "sent response to " + session.getRemoteAddress() + " : " + response));
            }
            default -> // ignore
            logger.error(new TechnicalLog(
                TechnicalLogEntity.Component.BACKEND, "tried to send an unknown packet"));
          }
        },
        chargepointRepository,
        firmwareRepository,
        logger);
  }
}
