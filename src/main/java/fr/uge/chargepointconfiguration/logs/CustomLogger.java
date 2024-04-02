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

import fr.uge.chargepointconfiguration.logs.sealed.BusinessLog;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.Log;
import fr.uge.chargepointconfiguration.logs.sealed.LogEntity;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLog;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A CustomLogger storing and displaying differents logs.
 */
@Component
public class CustomLogger {

  private final Logger logger = LogManager.getLogger("BRS-Configurator");

  private final CustomLoggerService customLoggerService;

  /**
   * CustomLogger's constructor.
   *
   * @param customLoggerService A CustomLogService.
   */
  @Autowired
  CustomLogger(CustomLoggerService customLoggerService) {
    this.customLoggerService = customLoggerService;
  }

  private void log(LogEntity logEntity) {
    var savedLog = customLoggerService.save(logEntity);
    logger.log(Level.getLevel(logEntity.getLevel()), savedLog.text());
  }

  /**
   * Store a log with a custom level, store it in database and then display it.
   *
   * @param level The log going to be stored and displayed.
   * @param log   The log going to be stored and displayed.
   */
  public void log(Level level, Log log) {
    switch (log) {
      case BusinessLog businessLog -> log(new BusinessLogEntity(
          businessLog.user(),
          businessLog.chargepoint(),
          businessLog.category(),
          level.name(),
          businessLog.completeLog()));
      case TechnicalLog technicalLog -> log(new TechnicalLogEntity(
          technicalLog.component(), level.name(), technicalLog.completeLog()));
    }
  }

  /**
   * Store a log with a fatal level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void fatal(Log log) {
    log(Level.FATAL, log);
  }

  /**
   * Store a log with an error level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void error(Log log) {
    log(Level.ERROR, log);
  }

  /**
   * Store a log with a warn level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void warn(Log log) {
    log(Level.WARN, log);
  }

  /**
   * Store a log with an info level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void info(Log log) {
    log(Level.INFO, log);
  }

  /**
   * Store a log with a debug level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void debug(Log log) {
    log(Level.DEBUG, log);
  }

  /**
   * Store a log with a trace level, store it in database and then display it.
   *
   * @param log The log going to be stored and displayed.
   */
  public void trace(Log log) {
    log(Level.TRACE, log);
  }
}
