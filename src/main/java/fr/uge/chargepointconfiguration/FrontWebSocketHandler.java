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
package fr.uge.chargepointconfiguration;

import fr.uge.chargepointconfiguration.chargepoint.notification.Notification;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Define the handler which manage clients websocket connection.
 */
@Component
public class FrontWebSocketHandler extends TextWebSocketHandler {
  private static final JsonParser jsonParser = new JsonParser();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock writeLock = lock.writeLock();
  private final Lock readLock = lock.readLock();
  private final List<WebSocketSession> usersSession = new ArrayList<>();

  /**
   * Call after a client websocket connection.
   *
   * @param session client websocket session
   */
  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    // Perform actions when a new WebSocket connection is established
    synchronized (lock) {
      usersSession.add(session);
    }
  }

  /**
   * Call when a session closed the websocket connection.
   *
   * @param session websocket user session
   * @param status  connection status
   * @throws Exception throw
   */
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    synchronized (lock) {
      usersSession.remove(session);
    }
  }

  /**
   * Send a text message to all clients connected to the websocket server.
   *
   * @param notificationMessage Websocket notification message
   */
  @EventListener
  public void sendMessageToUsers(Notification notificationMessage) {
    var textMessage = new TextMessage(jsonParser.objectToJsonString(notificationMessage));
    try {
      readLock.lock();
      usersSession.forEach(webSocketSession -> {
        try {
          webSocketSession.sendMessage(textMessage);
        } catch (IOException e) {
          System.out.println("Failed to sent a message to the client: " + e.getMessage());
        }
      });
    }
  }
}
