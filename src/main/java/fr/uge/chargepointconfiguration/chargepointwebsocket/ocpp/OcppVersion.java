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

import java.util.Objects;
import java.util.Optional;

/**
 * An enum to define the multiple version of the OCPP protocol.
 */
public enum OcppVersion {
  V1_6,
  V2_0_1;

  /**
   * Returns the correct version according to the given string.<br>
   * It will return an empty optional if the version could not be found.
   *
   * @param version A string sent by the websocket to defines the ocpp version.
   * @return An optional of the {@link OcppVersion}.
   */
  public static Optional<OcppVersion> parse(String version) {
    Objects.requireNonNull(version);
    return switch (version) {
      case "ocpp1.6" -> Optional.of(V1_6);
      case "ocpp2.0.1" -> Optional.of(V2_0_1);
      default -> Optional.empty();
    };
  }
}
