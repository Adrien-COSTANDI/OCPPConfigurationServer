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
package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.v16.ChangeConfiguration.ChangeConfigurationBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChangeConfigurationRequest16Test extends OcppBaseTest {

  @DisplayName("Should not throw an exception when instantiating the message")
  @Test
  public void correctConstructorShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      new ChangeConfigurationBuilder().withKey("key").withValue("value").build();
    });
  }

  @DisplayName("Should return the correct key")
  @Test
  public void returnsCorrectKey() {
    var test =
        new ChangeConfigurationBuilder().withKey("key").withValue("value").build();
    assertEquals("key", test.getKey());
  }

  @DisplayName("Should return the correct value")
  @Test
  public void returnsCorrectValue() {
    var test =
        new ChangeConfigurationBuilder().withKey("key").withValue("value").build();
    assertEquals("value", test.getValue());
  }

  @DisplayName("Should report a violation if the key is null")
  @Test
  public void invalidBeanIfKeyIsNull() {
    var test = new ChangeConfigurationBuilder().withValue("value").build();
    assertSingleViolation(test, "key");
  }

  @DisplayName("Should report a violation if the value is null")
  @Test
  public void invalidBeanIfValueIsNull() {
    var test = new ChangeConfigurationBuilder().withKey("key").build();
    assertSingleViolation(test, "value");
  }

  @DisplayName("Should report a violation if the key is more than 50 characters")
  @Test
  public void invalidBeanIfKeyIsIncorrect() {
    var test = new ChangeConfigurationBuilder()
        .withKey("My key is more than 50 characters, which is pretty long !")
        .withValue("value")
        .build();
    assertSingleViolation(test, "key");
  }

  @DisplayName("Should report a violation if the value is more than 500 characters")
  @Test
  public void invalidBeanIfValueIsIncorrect() {
    var test = new ChangeConfigurationBuilder()
        .withKey("key")
        .withValue(
            """
                    Unfortunately, the value is more than 500 characters long.
                    Furthermore, it should be not accepted as a value according to
                    the OCPP 1.6 specification edited in 2019...
                    Please, restrain yourself from putting a long value into the packet
                    or the constructor would not be happy with you.
                    You really do not want to have the constructor angry at you and if it is the case,
                    well, farewell and good luck my friend, because you'll need luck to survive !
                    Oh no, the constructor is here... Please have mercy, NOOOOOOOOOOOOOOOOOOOOOO
            """)
        .build();
    assertSingleViolation(test, "value");
  }
}
