package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.ChangeConfigurationResponse.Status.ACCEPTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfigurationResponse.ChangeConfigurationResponseBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChangeConfigurationResponse16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new ChangeConfigurationResponseBuilder().withStatus(ACCEPTED).build();
    });
  }

  @DisplayName("Should return the correct status")
  @Test
  public void returnsCorrectStatus() {
    var test = new ChangeConfigurationResponseBuilder().withStatus(ACCEPTED).build();
    assertIsValid(test);
    assertEquals(ACCEPTED, test.getStatus());
  }

  @DisplayName("Should report a violation if the status is null")
  @Test
  public void invalidBeanIfStatusIsNull() {
    var test = new ChangeConfigurationResponseBuilder().build();
    assertSingleViolation(test, "status");
  }
}
