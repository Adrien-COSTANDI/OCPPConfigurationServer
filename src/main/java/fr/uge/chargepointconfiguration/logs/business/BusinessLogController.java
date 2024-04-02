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

import fr.uge.chargepointconfiguration.logs.sealed.BusinessLogEntity;
import fr.uge.chargepointconfiguration.shared.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for business log.
 */
@RequestMapping("/api/log/business")
@RestController
@Tag(name = "Business Log", description = "The business log API")
public class BusinessLogController {

  private final BusinessLogService businessLogService;

  /**
   * BusinessLogController's constructor.
   *
   * @param businessLogService A BusinessLogService.
   */
  @Autowired
  public BusinessLogController(BusinessLogService businessLogService) {
    this.businessLogService = businessLogService;
  }

  /**
   * Returns a list of business logs according to the given chargepoint.
   *
   * @param id the id of the chargepoint.
   * @return a list of business logs by chargepoint.
   */
  @Operation(summary = "Get a list of logs by its charge point")
  @ApiResponse(
      responseCode = "200",
      description = "Found the list of business logs",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessLogDto.class)))
  @GetMapping(value = "/{id}")
  @PreAuthorize("hasRole('VISUALIZER')")
  public List<BusinessLogDto> getBusinessLogByChargepointId(@Parameter @PathVariable int id) {
    return businessLogService.getAllByChargepointId(id).stream()
        .map(BusinessLogEntity::toDto)
        .toList();
  }

  /**
   * Returns a list of technical logs.
   *
   * @return A list of technical logs.
   */
  @Operation(summary = "Search for business logs")
  @ApiResponse(
      responseCode = "200",
      description = "Found business logs",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessLogDto.class))
      })
  @GetMapping(value = "/search")
  @PreAuthorize("hasRole('VISUALIZER')")
  public PageDto<BusinessLogDto> getPage(
      @Parameter(description = "Desired size of the requested page.")
          @RequestParam(required = false, defaultValue = "10")
          int size,
      @Parameter(description = "Requested page.")
          @RequestParam(required = false, defaultValue = "0")
          int page,
      @Parameter(
              description =
                  "The column you want to sort by. Must be an attribute of the business log.")
          @RequestParam(required = false, defaultValue = "id")
          String sortBy,
      @Parameter(description = "The order of the sort. must be \"asc\" or \"desc\"")
          @RequestParam(required = false, defaultValue = "asc")
          String order,
      @Parameter(description = "The request used to search.")
          @RequestParam(required = false, defaultValue = "")
          String request) {
    var total = businessLogService.countTotalWithFilter(request);
    var totalElement = businessLogService.count();

    var data = businessLogService
        .search(
            request, PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy)))
        .stream()
        .map(BusinessLogEntity::toDto)
        .toList();

    return new PageDto<>(total, totalElement, page, size, data);
  }
}
