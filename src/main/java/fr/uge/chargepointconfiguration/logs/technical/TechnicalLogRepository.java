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
package fr.uge.chargepointconfiguration.logs.technical;

import fr.uge.chargepointconfiguration.logs.sealed.TechnicalLogEntity;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for technical log.
 */
@Repository
public interface TechnicalLogRepository
    extends CrudRepository<TechnicalLogEntity, Integer>,
        PagingAndSortingRepository<TechnicalLogEntity, Integer>,
        JpaSpecificationExecutor<TechnicalLogEntity> {

  /**
   * Method to return all technical logs.
   *
   * @return a list of all technical logs.
   */
  List<TechnicalLogEntity> findAllByOrderByIdDesc();

  Page<TechnicalLogEntity> findAllByOrderByIdDesc(Pageable pageable);

  /**
   * Method to return all technical logs by the component.
   *
   * @param component the type of component in the system.
   * @param level {@link Level}
   * @return the list of technical logs by component.
   */
  List<TechnicalLogEntity> findAllByComponentAndLevelOrderByIdDesc(
      TechnicalLogEntity.Component component, String level);

  /**
   * Method to return all technical logs by the criticality.
   *
   * @param level {@link Level}
   * @return the list of technical logs by criticality.
   */
  List<TechnicalLogEntity> findAllByLevelOrderByIdDesc(String level);
}
