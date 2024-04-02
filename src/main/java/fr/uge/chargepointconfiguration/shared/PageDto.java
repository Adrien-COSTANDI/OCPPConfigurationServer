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

import java.util.List;
import java.util.Objects;

/**
 * A record to represent a page of data.
 *
 * @param total total amount of element
 * @param page which page is displayed
 * @param size asked size of the page (not necessarily effective size)
 * @param data list of T containing the actual data
 *
 * @param <T> The type of data it is containing
 */
public record PageDto<T>(long total, long totalElement, int page, int size, List<T> data) {

  /**
   * Default constructor for a page containing a list of T.
   *
   * @param total total amount of element
   * @param page which page is displayed
   * @param size asked size of the page (not necessarily effective size)
   * @param data list of T containing the actual data
   */
  public PageDto {
    Objects.requireNonNull(data);
    if (total < 0 || page < 0 || size < 0) {
      throw new IllegalArgumentException("Illegal negative value.");
    }
  }
}
