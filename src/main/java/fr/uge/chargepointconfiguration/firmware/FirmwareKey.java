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
package fr.uge.chargepointconfiguration.firmware;

import java.util.Objects;

/**
 * Depending on the firmware version, a key can have a different name.
 */
public enum FirmwareKey {
  LIGHT_INTENSITY("LightIntensity", "LightIntensity", "LightIntensity"),

  NETWORK_PROFILE("BackOffice-URL-wired", "BackOffice-URL-wired", "BackOfficeNetworkProfile1"),

  CHARGEPOINT_IDENTITY("Identity", "Identity", "Identity"),

  LOCAL_AUTH_LIST("LocalAuthListEnabled", "LocalAuthListEnabled", "LocalAuthListEnabled"),

  STATION_MAX_CURRENT("Station-MaxCurrent", "Station-MaxCurrent", "Station-MaxCurrent"),

  STATION_PASSWORD("PW-SetChargerPassword", "PW-SetChargerPassword", "PW-SetChargerPassword");

  private final String firmwareV4KeyName;
  private final String firmwareV5KeyName;
  private final String firmwareV6KeyName;

  /**
   * Enum's constructor. Should be private and not called outside this enum.
   *
   * @param firmwareV4KeyName The key's name in the firmware > 4.*.
   * @param firmwareV5KeyName The key's name in the firmware > 5.*.
   * @param firmwareV6KeyName The key's name in the firmware > 6.*.
   */
  FirmwareKey(String firmwareV4KeyName, String firmwareV5KeyName, String firmwareV6KeyName) {
    this.firmwareV4KeyName = Objects.requireNonNull(firmwareV4KeyName);
    this.firmwareV5KeyName = Objects.requireNonNull(firmwareV5KeyName);
    this.firmwareV6KeyName = Objects.requireNonNull(firmwareV6KeyName);
  }

  /**
   * According to the firmware version, returns the correct key's name.
   *
   * @param firmwareVersion The firmware version of the chargepoint.
   * @return The correct key's name.
   */
  public String getFirmwareKeyAccordingToVersion(String firmwareVersion) {
    var majorVersion = firmwareVersion.split("\\.")[0];
    return switch (majorVersion) {
      case "4" -> firmwareV4KeyName;
      case "5" -> firmwareV5KeyName;
      case "6" -> firmwareV6KeyName;
      default -> throw new IllegalStateException("Unknown firmware");
    };
  }

  /**
   * According to the firmware version, returns the correct value's name.
   *
   * @param firmwareVersion The firmware version of the chargepoint.
   * @return The correct value's name.
   */
  public String getValueFormatAccordingToVersion(String firmwareVersion) {
    var majorVersion = firmwareVersion.split("\\.")[0];
    return switch (majorVersion) {
      case "4", "5" -> "%s";
      case "6" -> "ocppVersion{OCPP16}ocppCsmsUrl{%s}"
          + "messageTimeout{10}securityProfile{0}ocppInterface{Wired0}";
      default -> throw new IllegalStateException("Unknown firmware");
    };
  }
}
