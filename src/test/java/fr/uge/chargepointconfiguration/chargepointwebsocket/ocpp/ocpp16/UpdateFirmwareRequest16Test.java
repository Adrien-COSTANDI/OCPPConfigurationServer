package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

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
