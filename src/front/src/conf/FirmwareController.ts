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

import { CreateFirmwareFormData } from "../pages/home/firmware/CreateFirmware"

// TypeAllowed type definition
export type TypeAllowed = {
    id: number,
    constructor: string,
    type: string
}

// Firmware type definition
export type Firmware = {
    id: number,
    url: string,
    version: string,
    constructor: string,
    typesAllowed: Set<TypeAllowed>
}

export async function getFirmware(id: number): Promise<Firmware | undefined> {
    let request = await fetch(`/api/firmware/${id}`)
    if (request.ok) {
        let content = await request.json()
        let firmware = content as Firmware
        if (firmware) {
            return firmware
        } else {
            console.error("Failed to fetch firmware with id :" + id, content)
        }
    } else {
        console.error("Fetch firmware failed, error code:" +  request.status)
    }
    return undefined
}

export async function postCreateFirmware(firmware: CreateFirmwareFormData): Promise<boolean> {
    let typesArray: TypeAllowed[] = []
    firmware.typesAllowed.forEach(item => {
        typesArray.push(item)
    })
    let request = await fetch(window.location.origin + "/api/firmware/create",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                version: firmware.version,
                url: firmware.url,
                constructor: firmware.constructor,
                typesAllowed: typesArray,
            })
        })
    if (request.ok) {
        return true
    } else {
        console.error("Create firmware failed, error code:" + request.status)
        return false
    }
}

export async function updateFirmware(id: number, firmware: CreateFirmwareFormData): Promise<boolean> {
    let typesArray: TypeAllowed[] = []
    firmware.typesAllowed.forEach(item => {
        typesArray.push(item)
    })
    let request = await fetch(`/api/firmware/update/${id}`,
        {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                version: firmware.version,
                url: firmware.url,
                constructor: firmware.constructor,
                typesAllowed: typesArray,
            })
        })
    if (request.ok) {
        return true
    } else {
        console.error("Couldn't update firmware, error code: " + request.status)
        return false
    }
}

export async function getTypeAllowed(): Promise<TypeAllowed[] | undefined> {
    let request = await fetch(`/api/type/all`)
    if (request.ok) {
        let content = await request.json()
        let typesAllowed = content as TypeAllowed[]
        if (typesAllowed != null) {
            return typesAllowed
        }
    } else {
        console.error("Fetch type allowed list failed, error code:" +  request.status)
    }
    return undefined
}
