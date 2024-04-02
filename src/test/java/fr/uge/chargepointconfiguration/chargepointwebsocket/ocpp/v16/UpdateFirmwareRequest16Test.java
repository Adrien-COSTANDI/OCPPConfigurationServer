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

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.UpdateFirmware.UpdateFirmwareBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateFirmwareRequest16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new UpdateFirmwareBuilder()
          .withLocation(new URI("location"))
          .withRetrieveDate(Instant.now())
          .build();
    });
  }

  @DisplayName("Should return the correct location")
  @Test
  public void returnsCorrectLocation() throws URISyntaxException {
    var test = new UpdateFirmwareBuilder()
        .withLocation(new URI("location"))
        .withRetrieveDate(Instant.now())
        .build();
    assertIsValid(test);
    assertEquals("location", test.getLocation().toASCIIString());
  }

  @DisplayName("Should return the correct retrieve date")
  @Test
  public void returnsCorrectRetrieveDate() throws URISyntaxException {
    var date = Instant.now();
    var test = new UpdateFirmwareBuilder()
        .withLocation(new URI("location"))
        .withRetrieveDate(date)
        .build();
    assertIsValid(test);
    assertEquals(date, test.getRetrieveDate());
  }

  @DisplayName("Should report a violation if the location is null")
  @Test
  public void invalidBeanIfLocationIsNull() {
    var test = new UpdateFirmwareBuilder().withRetrieveDate(Instant.now()).build();
    assertSingleViolation(test, "location");
  }

  @DisplayName("Should report a violation if the retrieve date is null")
  @Test
  public void invalidBeanIfRetrieveDateIsNull() throws URISyntaxException {
    var test = new UpdateFirmwareBuilder().withLocation(new URI("location")).build();
    assertSingleViolation(test, "retrieveDate");
  }
}
