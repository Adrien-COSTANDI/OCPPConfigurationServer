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
package fr.uge.chargepointconfiguration.firmware;

import fr.uge.chargepointconfiguration.typeallowed.TypeAllowed;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Firmware class represents a firmware in the database via JPA.<br>
 * A Firmware has an ID, a URL, a majorVersion, a minorVersion, a constructor,
 * and some {@link TypeAllowed}.
 */
@Entity
@Table(name = "firmware")
public class Firmware {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_firmware")
  private int id;

  @Column(name = "url", nullable = false, length = 65_535)
  private String url;

  @Column(name = "version", nullable = false, length = 45)
  private String version;

  @Column(name = "constructor", nullable = false, length = 45)
  private String constructor;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "compatibility",
      joinColumns = @JoinColumn(name = "id_firmware"),
      inverseJoinColumns = @JoinColumn(name = "id_type_allowed"))
  private Set<TypeAllowed> typesAllowed;

  /**
   * Firmware's constructor.
   *
   * @param url Link where is the firmware stored.
   * @param version Firmware reference version.
   * @param constructor Manufacturer of this firmware.
   * @param  typesAllowed Set of all the compatible firmware.
   */
  public Firmware(String url, String version, String constructor, Set<TypeAllowed> typesAllowed) {
    this.url = Objects.requireNonNull(url);
    this.version = Objects.requireNonNull(version);
    this.constructor = Objects.requireNonNull(constructor);
    typesAllowed.forEach(Objects::requireNonNull);
    this.typesAllowed = Objects.requireNonNull(typesAllowed);
  }

  /**
   * Empty constructor. Should not be called.
   */
  public Firmware() {}

  public int getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getConstructor() {
    return constructor;
  }

  public void setConstructor(String constructor) {
    this.constructor = constructor;
  }

  public Set<TypeAllowed> getTypesAllowed() {
    return typesAllowed;
  }

  public void setTypesAllowed(Set<TypeAllowed> typesAllowed) {
    this.typesAllowed = typesAllowed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Firmware firmware = (Firmware) o;
    return id == firmware.id
        && Objects.equals(version, firmware.version)
        && Objects.equals(url, firmware.url)
        && Objects.equals(constructor, firmware.constructor)
        && Objects.equals(typesAllowed, firmware.typesAllowed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, url, version, constructor, typesAllowed);
  }

  public FirmwareDto toDto() {
    return new FirmwareDto(
        id,
        url,
        version,
        constructor,
        typesAllowed.stream().map(TypeAllowed::toDto).collect(Collectors.toSet()));
  }

  @Override
  public String toString() {
    return "Firmware{"
        + "id=" + id
        + ", url='" + url + '\''
        + ", version=" + version
        + ", constructor='" + constructor + '\''
        + ", typesAllowed=" + typesAllowed
        + '}';
  }
}
