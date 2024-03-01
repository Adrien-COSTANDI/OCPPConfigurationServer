package fr.uge.chargepointconfiguration.configuration;

import fr.uge.chargepointconfiguration.shared.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for the Configuration entity.
 */
@RestController
@RequestMapping("/api/configuration")
@Tag(name = "Configuration", description = "The configuration API")
public class ConfigurationController {

  private final ConfigurationService configurationService;

  /**
   * ConfigurationController's constructor.
   *
   * @param configurationService A ConfigurationService doing database manipulations.
   */
  @Autowired
  public ConfigurationController(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  /**
   * Returns a list of all configuration without the configuration.
   *
   * @return A list of all the configuration.
   */
  @Operation(summary = "Get all configuration")
  @ApiResponse(responseCode = "200",
          description = "Found all the configuration.",
          content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationGeneralDto.class)
          )
  )
  @GetMapping(value = "/all")
  public List<ConfigurationGeneralDto> getAllConfiguration() {
    return configurationService.getAllConfigurations();
  }

  /**
   * Return the configuration corresponding to the id parameter.
   *
   * @return The corresponding configuration.
   */
  @Operation(summary = "Get configuration by its id")
  @ApiResponses(value = { @ApiResponse(responseCode = "200",
          description = "Found the corresponding configuration",
          content = { @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationDto.class)) }), @ApiResponse(
          responseCode = "404",
          description = "This configuration does not exist",
          content = @Content) })
  @GetMapping(value = "/{id}")
  public Optional<ConfigurationDto> getConfigurationById(
          @Parameter(description = "Id of the configuration your are looking for.")
          @PathVariable int id) {
    return configurationService.getConfiguration(id);
  }

  /**
   * Create a configuration.
   *
   * @param createConfigurationDto All the necessary information for a configuration creation.
   * @return A configuration created with its information and its http result status.
   */
  @Operation(summary = "Create a configuration")
  @ApiResponses(value = { @ApiResponse(responseCode = "201",
          description = "Configuration created",
          content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationDto.class)
          )
        )
  })
  @PostMapping("/create")
  public ResponseEntity<ConfigurationDto> registerConfiguration(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "The configuration to be sent to the controller.",
            required = true,
            content = @Content(
                  examples = @ExampleObject(
                        """
                        {
                          "name": "the name of the configuration",
                          "description": "A short description about the configuration",
                          "configuration": "{key1: value1, key2: value2}",
                          "firmware": 0
                        }
                        """
                  )
            )
      )
      @RequestBody CreateConfigurationDto createConfigurationDto) {
    return new ResponseEntity<>(configurationService.save(createConfigurationDto),
        HttpStatus.CREATED);
  }

  /**
   * Update a configuration.
   *
   * @param updateConfigurationDto All the necessary information for a configuration update.
   * @return A configuration created with its information and its http result status.
   */
  @Operation(summary = "Update a configuration")
  @ApiResponses(value = { @ApiResponse(responseCode = "201",
          description = "Configuration updated",
          content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationDto.class)
          )
      )
  })
  @PostMapping("/update")
  public ResponseEntity<ConfigurationDto> updateConfiguration(
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  description = "The configuration to be sent to the controller.",
                  required = true,
                  content = @Content(
                          examples = @ExampleObject(
                                  """
                                  {
                                    "id": "id of the configuration to be updated"
                                    "name": "the name of the configuration",
                                    "description": "A short description about the configuration",
                                    "configuration": "{key1: value1, key2: value2}",
                                    "firmware": 0
                                  }
                                  """
                          )
                  )
          )
          @RequestBody UpdateConfigurationDto updateConfigurationDto) {
    var configuration = configurationService.update(updateConfigurationDto);
    return configuration
            .map(configurationDto -> new ResponseEntity<>(configurationDto, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
  }


  /**
   * Returns a list of {@link ConfigurationTranscriptor}.
   *
   * @return list of {@link ConfigurationTranscriptor}.
   */
  @Operation(summary = "Get all fullName key configuration")
  @ApiResponse(responseCode = "200",
          description = "Found all fullName key configuration.",
          content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationTranscriptorDto.class)
          )
  )
  @GetMapping(value = "/transcriptor")
  public List<ConfigurationTranscriptorDto> getAllConfigurationTranscriptor() {
    return Arrays.stream(ConfigurationTranscriptor.values())
            .map(ConfigurationTranscriptor::toDto)
            .toList();
  }

  /**
   * Search for {@link ConfigurationDto} with a pagination.
   *
   * @param size   Desired size of the requested page.
   * @param page   Requested page.
   * @param sortBy The column you want to sort by. Must be an attribute of
   *               the {@link ConfigurationDto}.
   * @param order  The order of the sort. Must be "asc" or "desc".
   * @return A page containing a list of {@link ConfigurationDto}
   */
  @Operation(summary = "Search for configurations")
  @ApiResponse(responseCode = "200",
          description = "Found configurations",
          content = { @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ConfigurationDto.class))
          })
  @GetMapping(value = "/search")
  public PageDto<ConfigurationDto> getPage(
          @Parameter(description = "Desired size of the requested page.")
          @RequestParam(required = false, defaultValue = "10") int size,

          @Parameter(description = "Requested page.")
          @RequestParam(required = false, defaultValue = "0") int page,

          @Parameter(description =
                  "The column you want to sort by. Must be an attribute of the configuration.")
          @RequestParam(required = false, defaultValue = "id") String sortBy,

          @Parameter(description = "The order of the sort. must be \"asc\" or \"desc\"")
          @RequestParam(required = false, defaultValue = "asc") String order
  ) {
    var total = configurationService.countTotal();

    var data = configurationService.getPage(
                    PageRequest.of(page, size, Sort.by(Sort.Order.by(order).getDirection(), sortBy))
            ).stream()
            .map(entity -> new ConfigurationDto(entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getLastEdit(),
                    entity.getConfiguration(),
                    entity.getFirmware().toDto()
            ))
            .toList();

    return new PageDto<>(total, page, size, data);
  }
}