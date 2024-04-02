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
package fr.uge.chargepointconfiguration.logs;

import fr.uge.chargepointconfiguration.logs.business.BusinessLogRepository;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.LogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import fr.uge.chargepointconfiguration.logs.technical.TechnicalLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A CustomLoggerService doing database manipulations.
 */
@Service
public class CustomLoggerService {

  private final BusinessLogRepository businessLogRepository;

  private final TechnicalLogRepository technicalLogRepository;

  /**
   * CustomLoggerService's constructor.
   *
   * @param businessLogRepository A BusinessLogRepository accessing to database.
   * @param technicalLogRepository A TechnicalLogRepository accessing to database.
   */
  @Autowired
  public CustomLoggerService(
      BusinessLogRepository businessLogRepository, TechnicalLogRepository technicalLogRepository) {
    this.businessLogRepository = businessLogRepository;
    this.technicalLogRepository = technicalLogRepository;
  }

  /**
   * Saves a log into the database in the corresponding table according to the log type.
   *
   * @param log {@link LogEntity}
   * @return The used log.
   */
  public LogEntity save(LogEntity log) {
    return switch (log) {
      case BusinessLogEntity businessLogEntity -> businessLogRepository.save(businessLogEntity);
      case TechnicalLogEntity technicalLogEntity -> technicalLogRepository.save(technicalLogEntity);
    };
  }
}
