package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.FirmwareStatusNotificationResponse.FirmwareStatusNotificationResponseBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FirmwareStatusNotificationResponse16Test {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new FirmwareStatusNotificationResponseBuilder();
    });
  }
}
