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

/**
 * Represents the message's type.<br>
 * - 2 if it is a request ;<br>
 * - 3 if it is a response ;<br>
 * - 99 if it is unknown.
 */
public enum MessageType {
  REQUEST(2),
  RESPONSE(3),
  UNKNOWN(99);

  private final int callType;

  MessageType(int callType) {
    this.callType = callType;
  }

  /**
   * Returns the call type of the current enum.
   *
   * @return The current enum's call type.
   */
  public int getCallType() {
    return callType;
  }

  /**
   * By giving the callType, this method returns the correct enum.
   *
   * @param callType The message's call type.
   * @return The enum according to the call type.
   */
  public static MessageType codeToEnum(int callType) {
    return switch (callType) {
      case 2 -> REQUEST;
      case 3 -> RESPONSE;
      default -> UNKNOWN;
    };
  }
}
