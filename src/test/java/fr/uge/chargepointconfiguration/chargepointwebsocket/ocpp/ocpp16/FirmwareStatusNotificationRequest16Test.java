package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.FirmwareStatusNotification.Status.INSTALLED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.FirmwareStatusNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FirmwareStatusNotificationRequest16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new FirmwareStatusNotification.FirmwareStatusNotificationBuilder()
          .withStatus(INSTALLED)
          .build();
    });
  }

  @DisplayName("Should return the correct status")
  @Test
  public void returnsCorrectStatus() {
    var test = new FirmwareStatusNotification.FirmwareStatusNotificationBuilder()
        .withStatus(INSTALLED)
        .build();
    assertIsValid(test);
    assertEquals(INSTALLED, test.getStatus());
  }

  @DisplayName("Should report a violation if the status is null")
  @Test
  public void invalidBeanIfStatusIsNull() {
    var test = new FirmwareStatusNotification.FirmwareStatusNotificationBuilder().build();
    assertSingleViolation(test, "status");
  }
}
