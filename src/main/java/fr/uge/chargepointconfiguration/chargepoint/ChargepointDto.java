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

import fr.uge.chargepointconfiguration.configuration.ConfigurationDto;
import fr.uge.chargepointconfiguration.status.StatusDto;

/**
 * DTO to read chargepoint in database.
 *
 * @param id Database id of the chargepoint stored.
 * @param serialNumberChargePoint The chargepoint's unique serial id.
 * @param type The commercial name of the chargepoint.
 * @param constructor The chargepoint's manufacturer.
 * @param clientId The client's name of the chargepoint.
 * @param configuration A JSON containing the chargepoint's configuration.
 * @param status {@link StatusDto}.
 */
public record ChargepointDto(
    int id,
    String serialNumberChargePoint,
    String type,
    String constructor,
    String clientId,
    ConfigurationDto configuration,
    StatusDto status) {}
