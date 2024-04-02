package fr.uge.chargepointconfiguration.chargepointwebsocket.ocpp.ocpp16;

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
