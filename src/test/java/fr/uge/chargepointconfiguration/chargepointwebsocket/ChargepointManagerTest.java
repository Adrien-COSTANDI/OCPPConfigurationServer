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
package fr.uge.chargepointconfiguration.chargepointwebsocket;

import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.ChangeConfigurationResponse.Status.ACCEPTED;
import static fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp_16.FirmwareStatusNotification.Status.INSTALLED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.uge.chargepointconfiguration.chargepoint.Chargepoint;
import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.MessageType;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppVersion;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.BootNotification.BootNotificationBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.BootNotificationResponse;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfiguration;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfigurationResponse.ChangeConfigurationResponseBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.FirmwareStatusNotification.FirmwareStatusNotificationBuilder;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.Reset;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.UpdateFirmware;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.UpdateFirmwareResponse;
import fr.uge.chargepointconfiguration.configuration.ConfigurationTranscriptor;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import fr.uge.chargepointconfiguration.tools.JsonParser;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JUnit test class for the {@link ChargePointManager}.
 */
@SpringBootTest
public class ChargepointManagerTest {

  private static final JsonParser jsonParser = new JsonParser();

  @Autowired
  private Validator validator;

  @Autowired
  private ChargepointRepository chargepointRepository;

  @Autowired
  private FirmwareRepository firmwareRepository;

  @Autowired
  private CustomLogger customLogger;

  private ChargePointManager instantiate() {
    return new ChargePointManager(
        OcppVersion.V1_6,
        (ocppMessage, chargePointManager) -> {
          var violations = validator.validate(ocppMessage);
          if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid message:" + violations);
          }
          if (Objects.requireNonNull(OcppMessage.ocppMessageToMessageType(ocppMessage))
              == MessageType.REQUEST) {
            var request = new WebSocketRequestMessage(
                MessageType.REQUEST.getCallType(),
                chargePointManager.getCurrentId(),
                WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(ocppMessage),
                jsonParser.objectToJsonString(ocppMessage));
            chargePointManager.setPendingRequest(request);
          }
        },
        chargepointRepository,
        firmwareRepository,
        customLogger);
  }

  /**
   * Should not throw an exception while calling the constructor.
   */
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new ChargePointManager(
          OcppVersion.V1_6,
          (ocppMessage, chargePointManager) -> {
            var violations = validator.validate(ocppMessage);
            if (!violations.isEmpty()) {
              throw new IllegalArgumentException("Invalid message:" + violations);
            }
          },
          chargepointRepository,
          firmwareRepository,
          customLogger);
    });
  }

  /**
   * Should correctly change the current chargepoint entry in the database on error.
   */
  @Test
  public void onErrorShouldResultInAChangeInDatabase() {
    var chargepointManager = instantiate();
    var currentChargepoint = chargepointRepository.findAllByOrderByIdDesc().getFirst();
    chargepointManager.setCurrentChargepoint(currentChargepoint);
    var error = new Exception("An error for the test :)");
    chargepointManager.onError(error);
    var actualChargepoint = chargepointRepository.findAllByOrderByIdDesc().getFirst();
    assertEquals(currentChargepoint.getError(), actualChargepoint.getError());
    assertEquals(currentChargepoint.getStep(), actualChargepoint.getStep());
    assertEquals(currentChargepoint.isState(), actualChargepoint.isState());
    assertEquals(
        currentChargepoint.getSerialNumberChargePoint(),
        actualChargepoint.getSerialNumberChargePoint());
    assertEquals(currentChargepoint.getType(), actualChargepoint.getType());
    assertEquals(currentChargepoint.getClientId(), actualChargepoint.getClientId());
    assertEquals(currentChargepoint.getStatus(), actualChargepoint.getStatus());
  }

  /**
   * Should correctly change the current chargepoint entry in the database on disconnection.
   */
  @Test
  public void onDisconnectionShouldResultInAChangeInDatabase() {
    var chargepointManager = instantiate();
    var currentChargepoint = chargepointRepository.findAllByOrderByIdDesc().getFirst();
    chargepointManager.setCurrentChargepoint(currentChargepoint);
    chargepointManager.onDisconnection();
    var actualChargepoint = chargepointRepository.findAllByOrderByIdDesc().getFirst();
    assertEquals(currentChargepoint.getError(), actualChargepoint.getError());
    assertEquals(currentChargepoint.getStep(), actualChargepoint.getStep());
    assertEquals(currentChargepoint.isState(), actualChargepoint.isState());
    assertEquals(
        currentChargepoint.getSerialNumberChargePoint(),
        actualChargepoint.getSerialNumberChargePoint());
    assertEquals(currentChargepoint.getType(), actualChargepoint.getType());
    assertEquals(currentChargepoint.getClientId(), actualChargepoint.getClientId());
    assertEquals(currentChargepoint.getStatus(), actualChargepoint.getStatus());
  }

  /**
   * Should send a {@link BootNotificationResponse} with a rejected status.
   */
  @Test
  public void onMessageShouldRejectUnknownChargepoint() throws IOException {
    var chargepointManager = instantiate();
    var bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Testor Corp")
        .withChargePointModel("A big chargepoint")
        .withChargePointSerialNumber("ABCDE524154")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("5.5.5-5555")
        .build();
    var request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    var sentMessage = chargepointManager.processMessage(request);
    var actualResponse = (BootNotificationResponse) sentMessage.orElseThrow();
    assertEquals(BootNotificationResponse.class, sentMessage.orElseThrow().getClass());
    assertEquals(BootNotificationResponse.Status.REJECTED, actualResponse.getStatus());
  }

  /**
   * Should accept a known chargepoint even if it hasn't a configuration.
   */
  @Test
  public void onMessageShouldAcceptChargepointWithoutConfiguration() throws IOException {
    var chargepointManager = instantiate();
    var bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Alfen BV")
        .withChargePointModel("Borne to be alive")
        .withChargePointSerialNumber("ACE0000001")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("5.5.5-5555")
        .build();
    var request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    var sentMessage = chargepointManager.processMessage(request);
    var actualResponse = (BootNotificationResponse) sentMessage.orElseThrow();
    assertEquals(BootNotificationResponse.class, sentMessage.orElseThrow().getClass());
    assertEquals(BootNotificationResponse.Status.ACCEPTED, actualResponse.getStatus());
  }

  /**
   * In this test, the chargepoint should have {@link UpdateFirmware} sent
   * until it is done.
   */
  @Test
  public void onMessageShouldSendAUpdateFirmwareRequestUntilIsDone() throws IOException {
    var chargepointManager = instantiate();
    var bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Alfen BV")
        .withChargePointModel("Borne to be alive")
        .withChargePointSerialNumber("ACE0000005")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("5.5.5-5555")
        .build();
    var request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    var sentMessage = chargepointManager.processMessage(request);
    var actualResponse = (UpdateFirmware) sentMessage.orElseThrow();
    assertEquals(UpdateFirmware.class, actualResponse.getClass());
    assertEquals("https://lienFirmware2", actualResponse.getLocation().toASCIIString());
    var responseFromTheChargepoint = new UpdateFirmwareResponse();
    var response = new WebSocketResponseMessage(
        MessageType.RESPONSE.getCallType(),
        chargepointManager.getCurrentId(),
        jsonParser.objectToJsonString(responseFromTheChargepoint));
    sentMessage = chargepointManager.processMessage(response);
    assertThrows(NoSuchElementException.class, sentMessage::orElseThrow);
    var statusFromTheChargepoint =
        new FirmwareStatusNotificationBuilder().withStatus(INSTALLED).build();
    request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(statusFromTheChargepoint),
        jsonParser.objectToJsonString(statusFromTheChargepoint));
    sentMessage = chargepointManager.processMessage(request);
    var reset = (Reset) sentMessage.orElseThrow();
    assertEquals(Reset.class, sentMessage.orElseThrow().getClass());
    assertEquals(Reset.Type.HARD, reset.getType());
    bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Alfen BV")
        .withChargePointModel("Borne to be alive")
        .withChargePointSerialNumber("ACE0000005")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("5.8.1-4123")
        .build();
    request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    sentMessage = chargepointManager.processMessage(request);
    actualResponse = (UpdateFirmware) sentMessage.orElseThrow();
    assertEquals(UpdateFirmware.class, actualResponse.getClass());
    assertEquals("https://lienFirmware1", actualResponse.getLocation().toASCIIString());
    responseFromTheChargepoint = new UpdateFirmwareResponse();
    response = new WebSocketResponseMessage(
        MessageType.RESPONSE.getCallType(),
        chargepointManager.getCurrentId(),
        jsonParser.objectToJsonString(responseFromTheChargepoint));
    sentMessage = chargepointManager.processMessage(response);
    assertThrows(NoSuchElementException.class, sentMessage::orElseThrow);
    statusFromTheChargepoint =
        new FirmwareStatusNotificationBuilder().withStatus(INSTALLED).build();
    request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(statusFromTheChargepoint),
        jsonParser.objectToJsonString(statusFromTheChargepoint));
    sentMessage = chargepointManager.processMessage(request);
    reset = (Reset) sentMessage.orElseThrow();
    assertEquals(Reset.class, sentMessage.orElseThrow().getClass());
    assertEquals(Reset.Type.HARD, reset.getType());
    bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Alfen BV")
        .withChargePointModel("Borne to be alive")
        .withChargePointSerialNumber("ACE0000005")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("6.1.1-4160")
        .build();
    request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    sentMessage = chargepointManager.processMessage(request);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    assertEquals(
        Chargepoint.Step.CONFIGURATION,
        chargepointManager.getCurrentChargepoint().getStep());
    assertEquals(
        Chargepoint.StatusProcess.PROCESSING,
        chargepointManager.getCurrentChargepoint().getStatus());
  }

  /**
   * Should send {@link ChangeConfiguration}
   * until the configuration is done.
   */
  @Test
  public void onMessageShouldSendAChangeConfigurationRequestUntilIsDone() throws IOException {
    var chargepointManager = instantiate();
    var bootNotifMessage = new BootNotificationBuilder()
        .withChargePointVendor("Alfen BV")
        .withChargePointModel("Borne to be alive")
        .withChargePointSerialNumber("ACE0000002")
        .withChargeBoxSerialNumber("Leroy Jenkins")
        .withFirmwareVersion("5.5.5-5555")
        .build();
    var request = new WebSocketRequestMessage(
        MessageType.REQUEST.getCallType(),
        chargepointManager.getCurrentId(),
        WebSocketMessage.MessageTypeRequest.ocppMessageToEnum(bootNotifMessage),
        jsonParser.objectToJsonString(bootNotifMessage));
    var sentMessage = chargepointManager.processMessage(request);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    var actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
    assertEquals(
        ConfigurationTranscriptor.LIGHT_INTENSITY
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
        actualResponse.getKey());
    assertEquals("100", actualResponse.getValue());
    var responseFromTheChargepoint =
        new ChangeConfigurationResponseBuilder().withStatus(ACCEPTED).build();
    var response = new WebSocketResponseMessage(
        MessageType.RESPONSE.getCallType(),
        chargepointManager.getCurrentId(),
        jsonParser.objectToJsonString(responseFromTheChargepoint));
    sentMessage = chargepointManager.processMessage(response);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
    assertEquals(
        ConfigurationTranscriptor.CHARGEPOINT_IDENTITY
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
        actualResponse.getKey());
    assertEquals("Borne-Test", actualResponse.getValue());
    sentMessage = chargepointManager.processMessage(response);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
    assertEquals(
        ConfigurationTranscriptor.LOCAL_AUTH_LIST
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
        actualResponse.getKey());
    assertEquals("true", actualResponse.getValue());
    sentMessage = chargepointManager.processMessage(response);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
    assertEquals(
        ConfigurationTranscriptor.STATION_MAX_CURRENT
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
        actualResponse.getKey());
    assertEquals("20", actualResponse.getValue());
    sentMessage = chargepointManager.processMessage(response);
    assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
    actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
    assertEquals(
        ConfigurationTranscriptor.CHARGEPOINT_IDENTITY
            .getOcpp16Key()
            .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
        actualResponse.getKey());
    assertEquals("dépasse les bornes", actualResponse.getValue());
    if (System.getenv("FINAL_WS_SERVER_ADDRESS") != null) {
      sentMessage = chargepointManager.processMessage(response);
      assertEquals(ChangeConfiguration.class, sentMessage.orElseThrow().getClass());
      actualResponse = (ChangeConfiguration) sentMessage.orElseThrow();
      assertEquals(
          ConfigurationTranscriptor.NETWORK_PROFILE
              .getOcpp16Key()
              .getFirmwareKeyAccordingToVersion(bootNotifMessage.getFirmwareVersion()),
          actualResponse.getKey());
      assertEquals(System.getenv("FINAL_WS_SERVER_ADDRESS"), actualResponse.getValue());
    }
    sentMessage = chargepointManager.processMessage(response);
    assertEquals(Reset.class, sentMessage.orElseThrow().getClass());
    var resetRequest = (Reset) sentMessage.orElseThrow();
    assertEquals(Reset.Type.HARD, resetRequest.getType());
    sentMessage = chargepointManager.processMessage(response);
    var finalSentMessage = sentMessage;
    assertThrows(NoSuchElementException.class, finalSentMessage::orElseThrow);
    assertEquals(
        Chargepoint.StatusProcess.FINISHED,
        chargepointManager.getCurrentChargepoint().getStatus());
  }
}
