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

/**
 * Translate api role to french 
 * @param role api role to be translated
 * @returns A translated (into french) string.
 */
export function apiRoleToFrench(role: ApiRole): string {
    switch (role) {
        case "ADMINISTRATOR":
            return "Administrateur";
        case "EDITOR":
            return "Éditeur";
        case "VISUALIZER":
            return "Visualiseur";
        default:
            return "Inconnu";
    }
}

/**
 * Translate french role to api role 
 * @param role french role to be translated
 * @returns A translated (into french) string.
 */
export function frenchToEnglishRole(role: FrenchRole): string {
    switch (role) {
        case "Administrateur":
            return "ADMINISTRATOR";
        case "Éditeur":
            return "EDITOR";
        case "Visualiseur":
            return "VISUALIZER";
        default:
            return "Inconnu";
    }
}

export type ApiRole = "VISUALIZER" | "EDITOR" | "ADMINISTRATOR"
export type FrenchRole = "Administrateur" | "Éditeur" | "Visualiseur"

export type User = {
    id: number,
    email: string,
    lastName: string,
    firstName: string,
    password: string,
    role: ApiRole
}

export type UserInformation = {
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    role: ApiRole,
}

export type CreateUserDto = {
    email: string,
    lastName: string,
    firstName: string,
    password: string,
    role: ApiRole
}

export async function searchUser(
    size: number = 10,
    page: number = 0,
    filter?: {filterField: string, filterValue: string },
    sort?: { sortField: string, sortOrder: 'asc' | 'desc' }): Promise<PageRequest<User> | undefined> {
    let request = await fetch(window.location.origin + `/api/user/search?size=${size}&page=${page}`)
    if (request.ok) {
        let content = await request.json()
        let user = (content as PageRequest<User>)
        if (user != null) {
            return user
        }
    }
    return undefined
}
