package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.UpdateFirmwareResponse;
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
