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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for the firmware entity.
 */
@RequestMapping("/api/type")
@RestController
@Tag(name = "TypeAllowed", description = "The type allowed API")
public class TypeAllowedController {

  private final TypeAllowedService typeAllowedService;

  /**
   * FirmwareController's constructor.
   *
   * @param typeAllowedService A FirmwareRepository.
   */
  @Autowired
  public TypeAllowedController(TypeAllowedService typeAllowedService) {
    this.typeAllowedService = typeAllowedService;
  }

  /**
   * Returns a list of all type allowed.
   *
   * @return A list of all type allowed.
   */
  @Operation(summary = "Get all the type allowed")
  @ApiResponse(
      responseCode = "200",
      description = "Found all type allowed",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TypeAllowedDto.class))
      })
  @GetMapping(value = "/all")
  @PreAuthorize("hasRole('EDITOR')")
  public List<TypeAllowedDto> getAllTypeAllowed() {
    return typeAllowedService.getAll().stream().map(TypeAllowed::toDto).toList();
  }

  @Operation(summary = "Create a new type allowed")
  @ApiResponse(
      responseCode = "201",
      description = "Type allowed created",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TypeAllowedDto.class))
      })
  @PostMapping("/create")
  @PreAuthorize("hasRole('EDITOR')")
  public ResponseEntity<TypeAllowedDto> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "JSON with all parameters of the new type allowed.",
              required = true)
          @RequestBody
          CreateTypeAllowedDto createTypeAllowedDto) {
    return new ResponseEntity<>(
        typeAllowedService.save(createTypeAllowedDto).toDto(), HttpStatus.CREATED);
  }
}
