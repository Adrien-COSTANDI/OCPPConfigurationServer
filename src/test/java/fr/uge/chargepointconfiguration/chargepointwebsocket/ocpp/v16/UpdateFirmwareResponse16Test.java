package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateFirmwareResponse16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new UpdateFirmwareResponse();
    });
    assertIsValid(new UpdateFirmwareResponse());
  }
}
