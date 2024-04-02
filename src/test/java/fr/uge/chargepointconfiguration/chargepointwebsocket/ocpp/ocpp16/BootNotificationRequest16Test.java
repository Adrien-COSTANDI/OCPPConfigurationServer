package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.BootNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BootNotificationRequest16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new BootNotification.BootNotificationBuilder()
          .withChargePointVendor("chargePointVendor")
          .withChargePointModel("chargePointModel")
          .withChargePointSerialNumber("chargePointSerialNumber")
          .withChargeBoxSerialNumber("chargeBoxSerialNumber")
          .withFirmwareVersion("firmwareVersion")
          .build();
    });
  }

  @DisplayName("Should return the correct charge point vendor")
  @Test
  public void returnsCorrectChargePointVendor() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertEquals("chargePointVendor", test.getChargePointVendor());
  }

  @DisplayName("Should return the correct charge point model")
  @Test
  public void returnsCorrectChargePointModel() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertEquals("chargePointModel", test.getChargePointModel());
  }

  @DisplayName("Should return the correct charge point serial number")
  @Test
  public void returnsCorrectChargePointSerialNumber() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertEquals("chargePointSerialNumber", test.getChargePointSerialNumber());
  }

  @DisplayName("Should return the correct charge point serial number even if it is null")
  @Test
  public void returnsCorrectChargePointSerialNumberEvenIfNull() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertNull(test.getChargePointSerialNumber());
  }

  @DisplayName("Should return the correct charge point box serial number")
  @Test
  public void returnsCorrectChargePointBoxSerialNumber() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertEquals("chargeBoxSerialNumber", test.getChargeBoxSerialNumber());
  }

  @DisplayName("Should return the correct charge point box serial number even if it is null")
  @Test
  public void returnsCorrectChargePointBoxSerialNumberEvenIfNull() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertNull(test.getChargeBoxSerialNumber());
  }

  @DisplayName("Should return the correct charge point firmware version")
  @Test
  public void returnsCorrectChargePointFirmwareVersion() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertIsValid(test);
    assertEquals("firmwareVersion", test.getFirmwareVersion());
  }

  @DisplayName("Should return the correct charge point firmware version even if it is null")
  @Test
  public void returnsCorrectChargePointFirmwareVersionEvenIfNull() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .build();
    assertIsValid(test);
    assertNull(test.getFirmwareVersion());
  }

  @DisplayName("Should report a violation if the charge point vendor is null")
  @Test
  public void invalidBeanIfVendorIsNull() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargePointVendor");
  }

  @DisplayName("Should report a violation if the charge point model is null")
  @Test
  public void invalidBeanIfModelIsNull() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargePointModel");
  }

  @DisplayName("Should not throw an exception if the charge point serial number is null")
  @Test
  public void doesNotThrowExceptionIfSerialNumberIsNull() {
    assertDoesNotThrow(() -> new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build());
  }

  @DisplayName("Should not throw an exception if the charge point box serial number is null")
  @Test
  public void doesNotThrowExceptionIfBoxSerialNumberIsNull() {
    assertDoesNotThrow(() -> new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build());
  }

  @DisplayName("Should not throw an exception if the charge point firmware version is null")
  @Test
  public void doesNotThrowExceptionIfFirmwareVersionIsNull() {
    assertDoesNotThrow(() -> new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .build());
  }

  @DisplayName("Should report a violation if the charge point vendor is longer than 20 characters")
  @Test
  public void invalidBeanIfVendorIsIncorrect() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("More than 20 characters here")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargePointVendor");
  }

  @DisplayName("Should report a violation if the charge point model is longer than 20 characters")
  @Test
  public void invalidBeanIfModelIsIncorrect() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("More than 20 characters here")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargePointModel");
  }

  @DisplayName(
      "Should report a violation if the charge point serial number is longer than 25 characters")
  @Test
  public void invalidBeanIfSerialNumberIsIncorrect() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("More than 25 characters here")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargePointSerialNumber");
  }

  @DisplayName(
      "Should report a violation if the charge point box serial number is longer than 25 characters")
  @Test
  public void invalidBeanIfBoxSerialNumberIsIncorrect() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("More than 25 characters here")
        .withFirmwareVersion("firmwareVersion")
        .build();
    assertSingleViolation(test, "chargeBoxSerialNumber");
  }

  @DisplayName(
      "Should report a violation if the charge point firmware version is longer than 50 characters")
  @Test
  public void invalidBeanIfFirmwareVersionIsIncorrect() {
    var test = new BootNotification.BootNotificationBuilder()
        .withChargePointVendor("chargePointVendor")
        .withChargePointModel("chargePointModel")
        .withChargePointSerialNumber("chargePointSerialNumber")
        .withChargeBoxSerialNumber("chargeBoxSerialNumber")
        .withFirmwareVersion("More than 50 characters in this field, so it should not work !")
        .build();
    assertSingleViolation(test, "firmwareVersion");
  }
}
