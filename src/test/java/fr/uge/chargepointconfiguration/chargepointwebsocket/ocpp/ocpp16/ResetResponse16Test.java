package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.ResetResponse.Status.ACCEPTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.ResetResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResetResponse16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new ResetResponse.ResetResponseBuilder().withStatus(ACCEPTED).build();
    });
  }

  @DisplayName("Should return the correct status")
  @Test
  public void returnsCorrectStatus() {
    var test = new ResetResponse.ResetResponseBuilder().withStatus(ACCEPTED).build();
    assertIsValid(test);
    assertEquals(ACCEPTED, test.getStatus());
  }

  @DisplayName("Should report a violation if the status is null")
  @Test
  public void invalidBeanIfStatusIsNull() {
    var test = new ResetResponse.ResetResponseBuilder().build();
    assertSingleViolation(test, "status");
  }
}
