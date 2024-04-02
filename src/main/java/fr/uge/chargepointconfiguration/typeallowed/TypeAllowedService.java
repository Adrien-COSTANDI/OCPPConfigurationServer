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
package fr.uge.chargepointconfiguration.typeallowed;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TypeAllowedService manage database manipulations.
 */
@Service
public class TypeAllowedService {

  private final TypeAllowedRepository typeAllowedRepository;

  /**
   * TypeAllowedService's constructor.
   *
   * @param typeAllowedRepository A ConfigurationRepository accessing to database.
   */
  @Autowired
  public TypeAllowedService(TypeAllowedRepository typeAllowedRepository) {
    this.typeAllowedRepository = typeAllowedRepository;
  }

  /**
   * Get all type allowed entries from the database.
   *
   * @return The list of all type allowed.
   */
  public List<TypeAllowed> getAll() {
    return typeAllowedRepository.findAll();
  }

  /**
   * Create a new type allowed.
   *
   * @param typeAllowedDto the type allowed to be saved.
   * @return the saved type allowed
   */
  public TypeAllowed save(CreateTypeAllowedDto typeAllowedDto) {
    return typeAllowedRepository.save(
        new TypeAllowed(typeAllowedDto.constructor(), typeAllowedDto.type()));
  }
}
