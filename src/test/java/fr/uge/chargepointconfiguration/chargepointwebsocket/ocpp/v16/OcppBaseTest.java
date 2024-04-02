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

import static org.assertj.core.api.Assertions.assertThat;

import fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.OcppMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.util.function.Consumer;

public class OcppBaseTest {
  public static void assertSingleViolation(OcppMessage ocppMessage, String propertyPath) {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      var violations = validator.validate(ocppMessage);
      assertThat(violations).hasSize(1);
      assertThat(violations).first().satisfies((Consumer<ConstraintViolation<OcppMessage>>)
          bootNotificationConstraintViolation -> assertThat(
                  bootNotificationConstraintViolation.getPropertyPath().toString())
              .isEqualTo(propertyPath));
    }
  }

  public static void assertIsValid(OcppMessage ocppMessage) {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      var violations = validator.validate(ocppMessage);
      assertThat(violations).isEmpty();
    }
  }
}
