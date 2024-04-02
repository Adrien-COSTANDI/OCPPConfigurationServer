package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.Reset.Type.HARD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.Reset.ResetBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResetRequest16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new ResetBuilder().withType(HARD).build();
    });
  }

  @DisplayName("Should return the correct type")
  @Test
  public void returnsCorrectType() {
    var test = new ResetBuilder().withType(HARD).build();
    assertIsValid(test);
    assertEquals(HARD, test.getType());
  }

  @DisplayName("Should report a violation if the type is null")
  @Test
  public void invalidBeanIfTypeIsNull() {
    var test = new ResetBuilder().build();
    assertSingleViolation(test, "type");
  }
}
