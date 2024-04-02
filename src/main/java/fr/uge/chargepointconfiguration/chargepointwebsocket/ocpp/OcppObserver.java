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
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp;

import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ChargePointManager;
import fr.uge.chargepointconfiguration.chargepointwebsocket.OcppMessageSender;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.OcppConfigurationObserver16;
import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v201.OcppConfigurationObserver201;
import fr.uge.chargepointconfiguration.firmware.FirmwareRepository;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Interface to defines the message observer.<br>
 * It listens and processes the message sent by the chargepoint.
 */
public interface OcppObserver {

  /**
   * Instantiates the correct observer according to the {@link OcppVersion}.
   *
   * @param ocppVersion {@link OcppVersion}.
   * @param chargePointManager {@link ChargePointManager}.
   * @param ocppMessageSender {@link OcppMessageSender}.
   * @param chargepointRepository {@link ChargepointRepository}.
   * @param firmwareRepository {@link FirmwareRepository}.
   * @param logger {@link CustomLogger}.
   * @return {@link OcppObserver}.
   */
  static OcppObserver instantiateFromVersion(
      OcppVersion ocppVersion,
      ChargePointManager chargePointManager,
      OcppMessageSender ocppMessageSender,
      ChargepointRepository chargepointRepository,
      FirmwareRepository firmwareRepository,
      CustomLogger logger) {
    Objects.requireNonNull(ocppVersion);
    Objects.requireNonNull(chargePointManager);
    Objects.requireNonNull(ocppMessageSender);
    Objects.requireNonNull(chargepointRepository);
    Objects.requireNonNull(firmwareRepository);
    Objects.requireNonNull(logger);
    return switch (ocppVersion) {
      case V1_6 -> new OcppConfigurationObserver16(
          ocppMessageSender, chargePointManager, chargepointRepository, firmwareRepository, logger);
      case V2_0_1 -> new OcppConfigurationObserver201(
          ocppMessageSender, chargePointManager, chargepointRepository);
    };
  }

  /**
   * Does something when receiving a message.
   *
   * @param ocppMessage {@link OcppMessage}.
   * @return {@link OcppMessage}, the message sent in response.
   */
  Optional<OcppMessage> onMessage(OcppMessage ocppMessage) throws IOException;

  /**
   * Does something when a connection has been set.
   *
   * @param chargePointManager {@link ChargePointManager}.
   */
  void onConnection(ChargePointManager chargePointManager);

  /**
   * Does something when a disconnection has been done.
   *
   * @param chargePointManager {@link ChargePointManager}.
   */
  void onDisconnection(ChargePointManager chargePointManager) throws IOException;
}
