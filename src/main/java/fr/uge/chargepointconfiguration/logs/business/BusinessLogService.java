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
package fr.uge.chargepointconfiguration.logs.business;

import fr.uge.chargepointconfiguration.chargepoint.ChargepointRepository;
import fr.uge.chargepointconfiguration.errors.exceptions.BadRequestException;
import fr.uge.chargepointconfiguration.errors.exceptions.EntityNotFoundException;
import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.shared.SearchUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * A BusinessLogService doing database manipulations.
 */
@Service
public class BusinessLogService {

  private final BusinessLogRepository businessLogRepository;

  private final ChargepointRepository chargepointRepository;

  /**
   * BusinessLogService's constructor.
   *
   * @param businessLogRepository A BusinessLogRepository accessing to database.
   * @param chargepointRepository A ChargepointRepository accessing to database.
   */
  @Autowired
  public BusinessLogService(
      BusinessLogRepository businessLogRepository, ChargepointRepository chargepointRepository) {
    this.businessLogRepository = businessLogRepository;
    this.chargepointRepository = chargepointRepository;
  }

  /**
   * Returns a list of business logs filtered by a chargepoint id.
   *
   * @param chargepointId the id of the chargepoint.
   * @return a list of business logs by chargepoint.
   */
  public List<BusinessLogEntity> getAllByChargepointId(int chargepointId) {
    var chargepoint = chargepointRepository
        .findById(chargepointId)
        .orElseThrow(() -> new EntityNotFoundException("Aucune borne avec l'id " + chargepointId));
    return businessLogRepository.findAllByChargepointOrderByIdDesc(chargepoint);
  }

  /**
   * Count the number of entities with the constraint of the given request.
   *
   * @param request the request used to search
   * @return the amount of entities with the constraint of the given request
   */
  public long countTotalWithFilter(String request) {
    try {
      var condition = SearchUtils.computeSpecification(request, BusinessLogEntity.class);
      return businessLogRepository.count(condition);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Requête invalide pour les filtres : " + request, e);
    }
  }

  public long count() {
    return businessLogRepository.count();
  }

  /**
   * Search for {@link BusinessLogEntity} with a pagination.
   *
   * @param request the request used to search
   * @param pageable The page requested
   * @return the list of corresponding {@link BusinessLogEntity}
   */
  public List<BusinessLogEntity> search(String request, PageRequest pageable) {
    try {
      var condition = SearchUtils.computeSpecification(request, BusinessLogEntity.class);
      return businessLogRepository.findAll(condition, pageable).stream().toList();
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Requête invalide pour les filtres : " + request, e);
    }
  }
}
