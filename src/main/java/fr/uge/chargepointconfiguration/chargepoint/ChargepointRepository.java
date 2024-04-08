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

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the charge point.
 */
@Repository
public interface ChargepointRepository
    extends CrudRepository<Chargepoint, Integer>,
        PagingAndSortingRepository<Chargepoint, Integer>,
        JpaSpecificationExecutor<Chargepoint> {

  /**
   * Returns a Chargepoint from the database according to the serial number and vendor.
   *
   * @param serialNumber Chargepoint's unique serial number.
   * @param constructor Chargepoint's vendor.
   * @return The correct Chargepoint or null if the chargepoint couldn't be found.
   */
  Chargepoint findBySerialNumberChargePointAndConstructor(String serialNumber, String constructor);

  /**
   * Return a list of registered Chargepoints from database.
   *
   * @return A list of Chargepoints or an empty list if no chargepoints are registered.
   */
  List<Chargepoint> findAllByOrderByIdDesc();

  Page<Chargepoint> findAllByClientIdContainingIgnoreCaseOrderByIdDesc(
      Pageable pageable, String clientId);
}
