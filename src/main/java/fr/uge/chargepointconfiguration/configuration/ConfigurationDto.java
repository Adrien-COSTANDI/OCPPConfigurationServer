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
package fr.uge.chargepointconfiguration.configuration;

import fr.uge.chargepointconfiguration.firmware.Firmware;
import fr.uge.chargepointconfiguration.firmware.FirmwareDto;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DTO to read configuration in database.
 *
 * @param id Database id of the configuration stored.
 * @param name How you want your configuration to be named.
 * @param description Brieve description of the meaining of this configuration.
 * @param configuration A JSON containing key and values for your configuration.
 * @param firmware {@link Firmware}.
 */
public record ConfigurationDto(
    int id,
    String name,
    String description,
    Timestamp lastEdit,
    String configuration,
    FirmwareDto firmware) {

  /**
   * Replace id by fullname of keys.
   *
   * @param configuration the String JSON of the configuration.
   * @return the new String JSON of the configuration.
   */
  public static String replaceIntToKey(String configuration) {
    if ("{}".equals(configuration)) {
      return configuration;
    }
    var map = Arrays.stream(ConfigurationTranscriptor.values())
        .collect(Collectors.toMap(
            ConfigurationTranscriptor::getId, ConfigurationTranscriptor::getFullName));
    var pattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\"(.*?)\"");
    var matcher = pattern.matcher(configuration.substring(1, configuration.length() - 1));
    var sb = new StringBuilder();
    sb.append("{");

    var start = 0;
    while (matcher.find()) {
      sb.append(configuration.substring(start, matcher.start()));

      var numericKey = Integer.parseInt(matcher.group(1));
      var value = matcher.group(2);

      var replacement = map.getOrDefault(numericKey, matcher.group(1));

      sb.append("\"").append(replacement).append("\":").append("\"").append(value);

      start = matcher.end();
    }
    sb.append(configuration.substring(start));
    return sb.toString();
  }
}
