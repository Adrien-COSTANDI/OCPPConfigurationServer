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
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
