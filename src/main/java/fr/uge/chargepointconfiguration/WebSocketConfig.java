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

import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Defines the web socket configuration.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final OcppWebSocketHandler ocppWebSocketHandler;
  private final FrontWebSocketHandler frontWebSocketHandler;
  private final String websocketPath;

  public WebSocketConfig(
      OcppWebSocketHandler ocppWebSocketHandler,
      FrontWebSocketHandler frontWebSocketHandler,
      @Value("${websocket.path}") String frontWebSocketPath) {
    this.ocppWebSocketHandler = ocppWebSocketHandler;
    this.frontWebSocketHandler = frontWebSocketHandler;
    this.websocketPath = frontWebSocketPath;
  }

  /**
   * Register a new websocket handler.
   *
   * @param registry websocket handler registry.
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(ocppWebSocketHandler, "/ocpp/**")
        .addHandler(frontWebSocketHandler, websocketPath)
        .setAllowedOrigins("*"); // TODO: maybe check CORS
  }
}
