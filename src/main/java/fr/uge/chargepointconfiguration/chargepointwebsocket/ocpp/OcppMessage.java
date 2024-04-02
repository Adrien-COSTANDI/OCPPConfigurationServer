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
 * Interface used to define an OCPP message.
 */
public interface OcppMessage {

  /**
   * Tells if the given message is a REQUEST or a RESPONSE.<br>
   * It is useful in sense where the two types are not formatted the same.
   *
   * @param ocppMessage {@link OcppMessage}.
   * @return {@link MessageType}.
   */
  static MessageType ocppMessageToMessageType(OcppMessage ocppMessage) {
    return switch (ocppMessage) {
      case OcppMessageRequest ignored -> MessageType.REQUEST;
      case OcppMessageResponse ignored -> MessageType.RESPONSE;
      default -> MessageType.UNKNOWN;
    };
  }
}
