///
/// The MIT License
/// Copyright © 2024 LastProject-ESIEE
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in
/// all copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
/// THE SOFTWARE.
///

import { Configuration } from "./configurationController";

// Websocket notification for charge point status update
export type WebSocketChargePointNotification = {
    id: number,
    status: ChargePointStatus,
}

// Status type definition
export type ChargePointStatus = {
    error: string,
    state: boolean, // true: connected, false: disconnected
    step: 'FIRMWARE' | 'CONFIGURATION',
    status: "PENDING" | "PROCESSING" | "FINISHED" | "FAILED"
    lastUpdate: Date
}

// ChargePoint type definition
export type ChargePoint = {
    id: number,
    serialNumberChargepoint: string,
    type: string,
    constructor: string,
    clientId: string,
    configuration: Configuration,
    status: ChargePointStatus,
}

export type CreateChargepointDto = {
    serialNumber : string,
    type: string,
    constructor: string,
    clientId: string,
    configuration: number,
}

export async function updateChargepoint(id: number, chargepoint: CreateChargepointDto) {
    let request = await fetch(`/api/chargepoint/${id}`,
        {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(chargepoint)
        })
    if (request.ok) {
        return true
    } else {
        console.error("Couldn't update chargepoint, error code: " + request.status)
        return false
    }
}

export async function getChargepointById(id: number) {
    let request = await fetch(`/api/chargepoint/${id}`)
    if (request.ok) {
        let content = await request.json()
        let chargePoint = (content as ChargePoint)
        if(chargePoint != null){
            return chargePoint
        } else {
            console.error("Fetch chargepoint failed " + content)
        }
    } else {
        console.error("Fetch chargepoint failed, error code:" +  request.status)
    }
    return undefined
}