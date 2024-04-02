package fr.uge.chargepointconfiguration.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Objects;

/**
 * Parses a JSON.
 */
public class JsonParser {

  public static final String OCPP_RFC3339_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  private static final ObjectMapper mapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public JsonParser() {}

  /**
   * Transform object to formatted JSON string.
   *
   * @param object object to transform
   * @param <T>    type of the object to transform
   * @return the string formatted
   */
  public static <T> String objectToJsonString(T object) {
    Objects.requireNonNull(object);
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Unable to parse object to JSON string : " + e.getMessage(), e);
    }
  }

  /**
   * Transform a JSON string to object.
   *
   * @param type    type of the object
   * @param content json string that represent the object
   * @param <T>     type of the object generated
   * @return a new instance of object
   */
  public <T> T stringToObject(Class<T> type, String content) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(content);
    try {
      return mapper.readValue(content, type);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Unable to parse JSON string to object : " + e.getMessage(), e);
    }
  }
}
