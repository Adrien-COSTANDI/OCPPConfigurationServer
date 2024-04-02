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
package fr.uge.chargepointconfiguration.shared;

/**
 * A record to represent a criteria for a filter.
 *
 * @param key the name of the key to be tested
 * @param operation the {@link Operation} to test
 * @param value the value to be tested with the operation
 */
public record SearchCriteria(String key, Operation operation, Object value) {

  /**
   * Operations that can be performed for the tests of the criterias.
   */
  public enum Operation {
    MORE_THAN,
    LESS_THAN,
    CONTAINS,
    UNKNOWN;

    /**
     * Get an {@link Operation} from a string.<br>
     * Can be "<", ">" or ":"
     *
     * @param string the given string
     * @return The corresponding operation, UNKNOWN if unknown
     */
    public static Operation fromString(String string) {
      return switch (string) {
        case "<" -> LESS_THAN;
        case ">" -> MORE_THAN;
        case ":" -> CONTAINS;
        default -> UNKNOWN;
      };
    }
  }
}
