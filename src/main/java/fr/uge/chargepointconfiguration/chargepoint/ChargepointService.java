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
package fr.uge.chargepointconfiguration.chargepoint;

import static fr.uge.chargepointconfiguration.configuration.Configuration.NO_CONFIG_ID;

import fr.uge.chargepointconfiguration.configuration.Configuration;
import fr.uge.chargepointconfiguration.configuration.ConfigurationRepository;
import fr.uge.chargepointconfiguration.errors.exceptions.BadRequestException;
import fr.uge.chargepointconfiguration.errors.exceptions.EntityAlreadyExistingException;
import fr.uge.chargepointconfiguration.errors.exceptions.EntityNotFoundException;
import fr.uge.chargepointconfiguration.shared.SearchUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * A ChargepointService doing database manipulations.
 */
@Service
public class ChargepointService {

  private final ChargepointRepository chargepointRepository;

  private final ConfigurationRepository configurationRepository;

  /**
   * ChargepointService's constructor.
   *
   * @param chargepointRepository   A ChargepointRepository accessing to database.
   * @param configurationRepository A ConfigurationRepository accessing to database.
   */
  @Autowired
  public ChargepointService(
      ChargepointRepository chargepointRepository,
      ConfigurationRepository configurationRepository) {
    this.chargepointRepository = chargepointRepository;
    this.configurationRepository = configurationRepository;
  }

  /**
   * Create a chargepoint.
   *
   * @param createChargepointDto All the necessary information for a configuration creation.
   * @return A chargepoint created with its information.
   */
  public Chargepoint save(CreateChargepointDto createChargepointDto) {
    checkFieldsChargepoint(createChargepointDto);
    checkAlreadyExistingChargepoint(createChargepointDto);

    Configuration configuration;
    if (createChargepointDto.configuration() == NO_CONFIG_ID) {
      configuration = null;
    } else {
      configuration = configurationRepository
          .findById(createChargepointDto.configuration())
          .orElseThrow(() -> new EntityNotFoundException(
              "Aucune configuration avec l'id " + createChargepointDto.configuration()));
    }
    return chargepointRepository.save(new Chargepoint(
        createChargepointDto.serialNumberChargepoint(),
        createChargepointDto.type(),
        createChargepointDto.constructor(),
        createChargepointDto.clientId(),
        configuration));
  }

  private void checkAlreadyExistingChargepoint(CreateChargepointDto createChargepointDto) {
    var chargepoint = chargepointRepository.findBySerialNumberChargePointAndConstructor(
        createChargepointDto.serialNumberChargepoint(), createChargepointDto.constructor());

    if (chargepoint != null) {
      throw new EntityAlreadyExistingException(
          "Une borne utilise déjà ce numéro de série et constructeur : "
              + createChargepointDto.serialNumberChargepoint() + ", "
              + createChargepointDto.constructor());
    }
  }

  public List<Chargepoint> getAllChargepoints() {
    return chargepointRepository.findAllByOrderByIdDesc().stream().toList();
  }

  public Chargepoint getChargepointById(int id) {
    return chargepointRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pas de borne avec l'id : " + id));
  }

  /**
   * Search for chargepoints with a pagination.
   *
   * @param request  the request used to search
   * @param pageable The page requested
   * @return the list of corresponding chargepoint
   */
  public List<Chargepoint> search(String request, PageRequest pageable) {
    try {
      var condition = SearchUtils.computeSpecification(request, Chargepoint.class);
      return chargepointRepository.findAll(condition, pageable).stream().toList();
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Requête invalide pour les filtres : " + request, e);
    }
  }

  /**
   * Count the number of entities with the constraint of the given request.
   *
   * @param request the request used to search
   * @return the amount of entities with the constraint of the given request
   */
  public long countTotalWithFilter(String request) {
    try {
      var condition = SearchUtils.computeSpecification(request, Chargepoint.class);
      return chargepointRepository.count(condition);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Requête invalide pour les filtres : " + request, e);
    }
  }

  public long count() {
    return chargepointRepository.count();
  }

  /**
   * Update a chargepoint and returns the updated chargepoint.
   *
   * @param id        id of the chargepoint to update
   * @param newValues the new values chargepoint
   * @return the updated chargepoint
   */
  public Chargepoint update(int id, CreateChargepointDto newValues) {
    checkFieldsChargepoint(newValues);

    var chargepoint = chargepointRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pas de borne avec l'id : " + id));

    var existingByConstructorAndSerialNumber =
        chargepointRepository.findBySerialNumberChargePointAndConstructor(
            newValues.serialNumberChargepoint(), newValues.constructor());

    if (existingByConstructorAndSerialNumber != null
        && id != existingByConstructorAndSerialNumber.getId()
        && existingByConstructorAndSerialNumber
            .getSerialNumberChargePoint()
            .equals(newValues.serialNumberChargepoint())
        && existingByConstructorAndSerialNumber.getConstructor().equals(newValues.constructor())) {
      throw new EntityAlreadyExistingException(
          "Une borne utilise déjà ce numéro de série et constructeur : "
              + newValues.serialNumberChargepoint() + ", "
              + newValues.constructor());
    }

    chargepoint.setSerialNumberChargePoint(newValues.serialNumberChargepoint());
    chargepoint.setClientId(newValues.clientId());
    chargepoint.setConstructor(newValues.constructor());
    chargepoint.setType(newValues.type());
    if (newValues.configuration() == NO_CONFIG_ID) {
      chargepoint.setConfiguration(null);
    } else {
      chargepoint.setConfiguration(configurationRepository
          .findById(newValues.configuration())
          .orElseThrow(() -> new EntityNotFoundException(
              "Pas de configuration avec l'id : " + newValues.configuration())));
    }
    return chargepointRepository.save(chargepoint);
  }

  private static void checkFieldsChargepoint(CreateChargepointDto newValues) {
    if (newValues.serialNumberChargepoint().isBlank()
        || newValues.constructor().isBlank()
        || newValues.type().isBlank()
        || newValues.clientId().isBlank()) {
      throw new BadRequestException(
          "Constructeur, numéro de série, type " + "et identifiant client sont requis");
    }
  }
}
