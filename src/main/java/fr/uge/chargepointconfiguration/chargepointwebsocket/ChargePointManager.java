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

import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepoint.notification.Notification;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.MessageType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessageParser;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppObserver;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppVersion;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Manages the charge point by listening and sending messages to the charge point.
 */
public class ChargePointManager {
  private final ApplicationEventPublisher applicationEventPublisher;
  private final OcppMessageParser ocppMessageParser;
  private final ChargepointRepository chargepointRepository;
  private final OcppObserver ocppObserver;
  private long currentId = 1;
  private WebSocketMessage pendingRequest = null;
  private Chargepoint currentChargepoint = null;

  /**
   * ChargePointManager's constructor.
   *
   * @param ocppVersion           The version of the OCPP protocol (1.6 or 2.0.1).
   * @param ocppMessageSender     The websocket connection used to send data.
   * @param chargepointRepository The chargepoint's repository for database queries.
   */
  public ChargePointManager(
      ApplicationEventPublisher applicationEventPublisher,
      OcppVersion ocppVersion,
      OcppMessageSender ocppMessageSender,
      ChargepointRepository chargepointRepository,
      FirmwareRepository firmwareRepository,
      CustomLogger logger) {
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    this.ocppMessageParser = OcppMessageParser.instantiateFromVersion(ocppVersion);
    this.chargepointRepository = Objects.requireNonNull(chargepointRepository);
    this.ocppObserver = OcppObserver.instantiateFromVersion(
        Objects.requireNonNull(ocppVersion),
        this,
        ocppMessageSender,
        chargepointRepository,
        Objects.requireNonNull(firmwareRepository),
        Objects.requireNonNull(logger));
  }

  /**
   * Returns the current websocket message id.
   *
   * @return A long representing the current websocket
   */
  public long getCurrentId() {
    return currentId;
  }

  /**
   * Sets the current pending request.<br>
   * If the current pending request is set,
   * we now wait for a response from the chargepoint.
   *
   * @param pendingRequest The {@link WebSocketMessage} request sent to the chargepoint.
   */
  public void setPendingRequest(WebSocketMessage pendingRequest) {
    this.pendingRequest = pendingRequest;
  }

  public Chargepoint getCurrentChargepoint() {
    return currentChargepoint;
  }

  public void setCurrentChargepoint(Chargepoint currentChargepoint) {
    this.currentChargepoint = currentChargepoint;
  }

  /**
   * Processes the received websocket message according to the OCPP protocol.
   *
   * @param webSocketMessage The {@link WebSocketMessage} sent to our server.
   */
  public Optional<OcppMessage> processMessage(WebSocketMessage webSocketMessage)
      throws IOException {
    Objects.requireNonNull(webSocketMessage);
    Optional<OcppMessage> message;
    if (webSocketMessage.isRequest()) {
      message = ocppMessageParser.parseRequestMessage(webSocketMessage);
    } else {
      message = ocppMessageParser.parseResponseMessage(pendingRequest, webSocketMessage);
      pendingRequest = null;
    }
    if (message.isEmpty()) {
      return Optional.empty();
    }
    var ocppMessage = message.orElseThrow();
    // Weird message, ignore it.
    if (OcppMessage.ocppMessageToMessageType(ocppMessage) == MessageType.REQUEST) {
      currentId = webSocketMessage.messageId();
    } else {
      currentId += 1;
    }
    return ocppObserver.onMessage(message.orElseThrow());
  }

  /**
   * Does something when the sender has been disconnected.
   */
  public void onDisconnection() {
    if (currentChargepoint != null) {
      currentChargepoint.setState(false);
      chargepointRepository.save(currentChargepoint);
      notifyStatusUpdate();
      var notification = Notification.notificationOnDisconnect(currentChargepoint);
      applicationEventPublisher.publishEvent(notification);
    }
  }

  /**
   * Does something if there is an error.
   */
  public void onError(Throwable t) {
    if (currentChargepoint != null) {
      currentChargepoint.setError(t.toString());
      currentChargepoint.setStatus(Chargepoint.StatusProcess.FAILED);
      chargepointRepository.save(currentChargepoint);
      notifyStatusUpdate();
      var notification = Notification.notificationOnError(currentChargepoint);
      applicationEventPublisher.publishEvent(notification);
    }
  }

  /**
   * Notifies via the websocket the current status of the current {@link Chargepoint}.
   */
  public void notifyStatusUpdate() {
    var notification = Notification.notificationOnStatusChange(currentChargepoint);
    applicationEventPublisher.publishEvent(notification);
  }

  /**
   * Notifies via the websocket the current status of the current {@link Chargepoint}
   * for the toast in the front.
   */
  public void notifyProcess() {
    var notification = Notification.notificationOnFinishedProcess(currentChargepoint);
    if (notification.isPresent()) {
      applicationEventPublisher.publishEvent(notification.orElseThrow());
    }
  }

  /**
   * Notifies via the websocket the connection of a {@link Chargepoint}
   * for the toast in the front.
   */
  public void notifyOnConnection() {
    var notification = Notification.notificationOnConnection(currentChargepoint);
    applicationEventPublisher.publishEvent(notification);
  }
}
