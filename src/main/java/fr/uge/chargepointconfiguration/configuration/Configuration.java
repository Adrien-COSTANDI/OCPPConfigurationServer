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
package fr.uge.chargepointconfiguration.configuration;

import fr.uge.chargepointconfiguration.firmware.Firmware;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A configuration class representation in the database.
 * A configuration has an Id, a name, a description and
 * the JSON configuration.
 */
@Entity
@Table(name = "configuration")
public class Configuration {

  public static final int NO_CONFIG_ID = -1;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_configuration")
  private int id;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "description", nullable = false, columnDefinition = "longtext default ''")
  private String description = "";

  @Column(
      name = "last_edit",
      nullable = false,
      columnDefinition = "datetime default current_timestamp")
  @CreationTimestamp
  private LocalDateTime lastEdit;

  @Column(name = "configuration", nullable = false)
  private String configuration;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "id_firmware", referencedColumnName = "id_firmware")
  private Firmware firmware;

  /**
   * Configuration's constructor.
   *
   * @param name How you want your configuration to be named.
   * @param description Describe the meaning of this configuration.
   * @param configuration A JSON containing key and values for your configuration.
   * @param firmware The chargepoint's firmware.
   */
  public Configuration(String name, String description, String configuration, Firmware firmware) {
    this(name, configuration, firmware);
    this.description = Objects.requireNonNull(description);
  }

  /**
   * Configuration's constructor without defaults values.
   *
   * @param name How you want your configuration to be named.
   * @param configuration A JSON containing key and values for your configuration.
   */
  public Configuration(String name, String configuration, Firmware firmware) {
    this.name = Objects.requireNonNull(name);
    this.configuration = Objects.requireNonNull(configuration);
    lastEdit = LocalDateTime.now();
    this.firmware = Objects.requireNonNull(firmware);
  }

  /**
   *  Configuration's constructor without all values.
   *
   * @param id configuration id in the database.
   * @param name configuration name.
   * @param configuration configuration definition
   * @param firmware configuration firmware version
   */
  public Configuration(int id, String name, String configuration, Firmware firmware) {
    this.id = id;
    this.name = Objects.requireNonNull(name);
    this.configuration = Objects.requireNonNull(configuration);
    lastEdit = LocalDateTime.now();
    this.firmware = Objects.requireNonNull(firmware);
  }

  /**
   * Empty constructor. Should not be called.
   */
  public Configuration() {}

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getLastEdit() {
    return lastEdit;
  }

  public void setLastEdit(LocalDateTime lastEdit) {
    this.lastEdit = lastEdit;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public Firmware getFirmware() {
    return firmware;
  }

  public void setFirmware(Firmware firmware) {
    this.firmware = firmware;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var other = (Configuration) o;
    return id == other.id
        && Objects.equals(name, other.name)
        && Objects.equals(description, other.description)
        && Objects.equals(lastEdit, other.lastEdit)
        && Objects.equals(configuration, other.configuration)
        && Objects.equals(firmware, other.firmware);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, lastEdit, configuration, firmware);
  }

  public ConfigurationDto toDto() {
    return new ConfigurationDto(
        id,
        name,
        description,
        Timestamp.valueOf(lastEdit),
        ConfigurationDto.replaceIntToKey(configuration),
        firmware == null ? null : firmware.toDto());
  }

  @Override
  public String toString() {
    return "Configuration{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", lastEdit=" + lastEdit
        + ", configuration='" + configuration + '\''
        + ", firmware=" + firmware
        + '}';
  }
}
