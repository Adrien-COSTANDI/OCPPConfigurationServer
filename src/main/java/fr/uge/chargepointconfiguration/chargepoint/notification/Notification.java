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
package fr.uge.chargepointconfiguration.chargepoint.notification;

import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.status.StatusDto;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines the notification sent for the front through the websocket.
 *
 * @param name  The notification's name.
 * @param value {@link WebSocketNotification}.
 */
public record Notification(String name, WebSocketNotification value) {

  /**
   * {@link Notification}'s constructor.
   *
   * @param name  The notification's name.
   * @param value {@link WebSocketNotification}.
   */
  public Notification {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
  }

  /**
   * Prepares a notification for a connection.
   *
   * @param chargepoint {@link Chargepoint}.
   * @return {@link Notification}.
   */
  public static Notification notificationOnConnection(Chargepoint chargepoint) {
    var message = new CriticalityWebsocketNotification(
        chargepoint.getClientId(),
        CriticalityWebsocketNotification.Type.INFO,
        chargepoint.getClientId() + " connectée");
    return new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message);
  }

  /**
   * Prepares a notification for a disconnection.
   *
   * @param chargepoint {@link Chargepoint}.
   * @return {@link Notification}.
   */
  public static Notification notificationOnDisconnect(Chargepoint chargepoint) {
    var message = new CriticalityWebsocketNotification(
        chargepoint.getClientId(),
        CriticalityWebsocketNotification.Type.INFO,
        chargepoint.getClientId() + " déconnectée");
    return new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message);
  }

  /**
   * Prepares a notification for an error.
   *
   * @param chargepoint {@link Chargepoint}.
   * @return {@link Notification}.
   */
  public static Notification notificationOnError(Chargepoint chargepoint) {
    var message = new CriticalityWebsocketNotification(
        chargepoint.getClientId(),
        CriticalityWebsocketNotification.Type.ERROR,
        chargepoint.getClientId() + " a eu une erreur");
    return new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message);
  }

  /**
   * Prepares a notification for a finished process.<br>
   * The process can be in either state : FINISHED or FAILED.
   *
   * @param chargepoint {@link Chargepoint}.
   * @return {@link Notification}.
   */
  public static Optional<Notification> notificationOnFinishedProcess(Chargepoint chargepoint) {
    var currentStep = chargepoint.getStep();
    if (chargepoint.getStatus() == Chargepoint.StatusProcess.FAILED) {
      var message = new CriticalityWebsocketNotification(
          chargepoint.getClientId(),
          CriticalityWebsocketNotification.Type.ERROR,
          "Échec " + (currentStep == Chargepoint.Step.FIRMWARE ? "du " : "de ") + currentStep);
      return Optional.of(
          new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message));
    } else if (chargepoint.getStatus() == Chargepoint.StatusProcess.FINISHED) {
      var message = new CriticalityWebsocketNotification(
          chargepoint.getClientId(),
          CriticalityWebsocketNotification.Type.SUCCESS,
          "Réussite " + (currentStep == Chargepoint.Step.FIRMWARE ? "du " : "de ") + currentStep);
      return Optional.of(
          new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message));
    } else {
      if (currentStep == Chargepoint.Step.CONFIGURATION) {
        var message = new CriticalityWebsocketNotification(
            chargepoint.getClientId(),
            CriticalityWebsocketNotification.Type.SUCCESS,
            "Réussite du " + Chargepoint.Step.FIRMWARE);
        return Optional.of(
            new Notification(CriticalityWebsocketNotification.class.getSimpleName(), message));
      }
    }
    return Optional.empty();
  }

  /**
   * Prepares a notification for a changed status.
   *
   * @param chargepoint {@link Chargepoint}.
   * @return {@link Notification}.
   */
  public static Notification notificationOnStatusChange(Chargepoint chargepoint) {
    var statusDto = new StatusDto(
        Timestamp.valueOf(chargepoint.getLastUpdate()),
        chargepoint.getError(),
        chargepoint.isState(),
        chargepoint.getStep(),
        chargepoint.getStatus());
    var chargepointNotification =
        new ChargePointWebsocketNotification(chargepoint.getId(), statusDto);
    return new Notification(
        ChargePointWebsocketNotification.class.getSimpleName(), chargepointNotification);
  }
}
