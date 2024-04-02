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

import { PageRequest } from "../sharedComponents/DisplayTable"
import { Firmware, TypeAllowed } from "./FirmwareController";

// Configuration type definition
export type Configuration = {
    id: number,
    name: string,
    description: string,
    lastEdit: Date,
    configuration: string,
    firmware: Firmware
}

// Create 
export type CreateConfigurationData = {
    name: string,
    description: string,
    configuration: KeyValueConfiguration[],
    firmware: string,
}

/**
 * Default value to tell the backend there is no selected configuration for a chargepoint
 */
export var noConfig: Configuration = {
    description: "",
    configuration: "",
    firmware: {
        id: -1,
        version:"",
        url: "",
        constructor: "",
        typesAllowed: new Set<TypeAllowed>()
    },
    lastEdit: new Date(),
    id: -1,
    name: "Pas de configuration"
}

export type KeyValueConfiguration = {
    key: Transcriptor,
    value: string,
}

export type ErrorState = {
    name: string,
    firmware: string,
    description: string,
}

export type Transcriptor = {
    id: number,
    fullName: string,
    regex: string,
}

export async function getTranscriptors(): Promise<Transcriptor[] | undefined> {
    let request = await fetch("/api/configuration/transcriptor")
    if (request.ok) {
        let content = await request.json()
        let transcriptors = (content as Transcriptor[])
        if (transcriptors != null) {
            return transcriptors
        }
    }
    return undefined
}


export async function getAllConfigurations(): Promise<Configuration[] | undefined> {
    let request = await fetch(`/api/configuration/all`)
    if (request.ok) {
        let content = await request.json()
        let configuration = (content as Configuration[])
        if (configuration != null) {
            return configuration
        }
    }
    return undefined
}


export async function searchConfiguration(
    size: number = 10,
    page: number = 0,
    filter?: { filterField: string, filterValue: string },
    sort?: { sortField: string, sortOrder: 'asc' | 'desc' }): Promise<PageRequest<Configuration> | undefined> {
        let request = await fetch(window.location.origin + `/api/configuration/search?size=${size}&page=${page}`)
        if (request.ok) {
            let content = await request.json()
            let configuration = (content as PageRequest<Configuration>)
            if (configuration != null) {
                return configuration
            }
        }
        return undefined
}

export async function getConfiguration(id: number): Promise<Configuration | undefined> {
    let request = await fetch(window.location.origin + `/api/configuration/${id}`)
    if (request.ok) {
        let content = await request.json()
        let configuration = content as Configuration
        if (configuration != null) {
            return configuration
        }
    }
    return undefined
}

export async function postUpdateConfiguration(id: number, configurationData: CreateConfigurationData): Promise<boolean> {
    let myConfig = globalStateResponseFormatter(configurationData)

    let request = await fetch(`/api/configuration/${id}`,
        {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: configurationData.name,
                description: configurationData.description,
                configuration: myConfig,
                firmware: configurationData.firmware
            })
        })
    if (request.ok) {
        return true
    } else {
        return false
    }
}

export function globalStateResponseFormatter(configurationData : CreateConfigurationData){
    let myConfig = configurationData.configuration.map(keyValue => `"${keyValue.key.id}":"${keyValue.value}"`)
        .join(", ")

    myConfig = "{" + myConfig + "}"
    return myConfig
}