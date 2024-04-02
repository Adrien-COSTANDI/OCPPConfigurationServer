package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.BootNotificationResponse;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BootNotificationResponse16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new BootNotificationResponse.BootNotificationResponseBuilder()
          .withCurrentTime(Instant.now())
          .withInterval(1)
          .withStatus(BootNotificationResponse.Status.ACCEPTED)
          .build();
    });
  }

  @DisplayName("Should return the correct current time")
  @Test
  public void returnsCorrectTime() {
    var currentTime = Instant.now();
    var test = new BootNotificationResponse.BootNotificationResponseBuilder()
        .withCurrentTime(currentTime)
        .withInterval(1)
        .withStatus(BootNotificationResponse.Status.ACCEPTED)
        .build();
    assertIsValid(test);
    assertEquals(currentTime, test.getCurrentTime());
  }

  @DisplayName("Should return the correct interval")
  @Test
  public void returnsCorrectInterval() {
    var test = new BootNotificationResponse.BootNotificationResponseBuilder()
        .withCurrentTime(Instant.now())
        .withInterval(1)
        .withStatus(BootNotificationResponse.Status.ACCEPTED)
        .build();
    assertIsValid(test);
    assertEquals(1, test.getInterval());
  }

  @DisplayName("Should return the correct status")
  @Test
  public void returnsCorrectStatus() {
    var test = new BootNotificationResponse.BootNotificationResponseBuilder()
        .withCurrentTime(Instant.now())
        .withInterval(1)
        .withStatus(BootNotificationResponse.Status.ACCEPTED)
        .build();
    assertIsValid(test);
    assertEquals(BootNotificationResponse.Status.ACCEPTED, test.getStatus());
  }

  @DisplayName("Should report violation if the time is null")
  @Test
  public void invalidBeanIfTimeIsNull() {
    var test = new BootNotificationResponse.BootNotificationResponseBuilder()
        .withInterval(1)
        .withStatus(BootNotificationResponse.Status.ACCEPTED)
        .build();
    assertSingleViolation(test, "currentTime");
  }

  @DisplayName("Should report violation if the status is null")
  @Test
  public void invalidBeanIfStatusIsNull() {
    var test = new BootNotificationResponse.BootNotificationResponseBuilder()
        .withCurrentTime(Instant.now())
        .withInterval(1)
        .build();
    assertSingleViolation(test, "status");
  }
}
