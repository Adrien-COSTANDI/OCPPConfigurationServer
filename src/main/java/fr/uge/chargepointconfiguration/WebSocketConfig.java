package fr.uge.chargepointconfiguration;

import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppWebSocketHandler;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import jakarta.validation.Validator;
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

  private final ChargepointRepository chargepointRepository;
  private final FirmwareRepository firmwareRepository;
  private final Validator validator;
  private final CustomLogger logger;
  private final String websocketPath;

  public WebSocketConfig(
      ChargepointRepository chargepointRepository,
      FirmwareRepository firmwareRepository,
      Validator validator,
      CustomLogger logger,
      @Value("${websocket.path}") String websocketPath) {
    this.chargepointRepository = chargepointRepository;
    this.firmwareRepository = firmwareRepository;
    this.validator = validator;
    this.logger = logger;
    this.websocketPath = websocketPath;
  }

  /**
   * Register a new websocket handler.
   *
   * @param registry websocket handler registry.
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(
            new OcppWebSocketHandler(chargepointRepository, firmwareRepository, validator, logger),
            "/ocpp/**")
        .addHandler(new FrontWebSocketHandler(), websocketPath)
        .setAllowedOrigins("*"); // TODO: maybe check CORS
  }
}
