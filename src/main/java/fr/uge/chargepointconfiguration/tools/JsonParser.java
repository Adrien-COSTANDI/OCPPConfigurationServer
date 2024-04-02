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
