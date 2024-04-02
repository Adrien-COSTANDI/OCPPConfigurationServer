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
package fr.uge.chargepointconfiguration.errors;

import fr.uge.chargepointconfiguration.errors.exceptions.BadRequestException;
import fr.uge.chargepointconfiguration.errors.exceptions.EntityAlreadyExistingException;
import fr.uge.chargepointconfiguration.errors.exceptions.EntityNotFoundException;
import fr.uge.chargepointconfiguration.errors.exceptions.ForbiddenOperationException;
import fr.uge.chargepointconfiguration.errors.exceptions.IllegalOperationException;
import fr.uge.chargepointconfiguration.logs.CustomLogger;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLog;
import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Controller to handle errors with exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private final CustomLogger logger;

  @Autowired
  public GlobalExceptionHandler(CustomLogger logger) {
    this.logger = logger;
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorMessage> handleNotFoundException(RuntimeException ex) {
    logger.error(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(EntityAlreadyExistingException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ErrorMessage> handleEntityAlreadyExistingException(RuntimeException ex) {
    logger.error(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorMessage> handleBadRequestException(RuntimeException ex) {
    logger.warn(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorMessage> handleBadArguments(RuntimeException ex) {
    logger.error(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage("Arguments invalides"), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalOperationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorMessage> handleIllegalOperationException(RuntimeException ex) {
    logger.error(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorMessage> handleForbiddenOperationException(RuntimeException ex) {
    logger.error(new TechnicalLog(TechnicalLogEntity.Component.BACKEND, ex.getMessage()));
    return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.FORBIDDEN);
  }
}
