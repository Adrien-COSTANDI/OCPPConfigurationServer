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


/**
 * Data model definition of a button in the nav bar
 */
export interface ButtonData {
    roles: string[];
    label: string;
    href: string;
    subButtons: ButtonData[];
}

/**
 * Define all nav bar buttons and theirs restrictions based on the user role 
 */
export const buttons: ButtonData[] = [
    {
        roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
        label: "Configuration",
        href: "/configuration",
        subButtons: [
            {
                roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
                label: "Afficher/modifier",
                href: "",
                subButtons: []
            },
            {
                roles: ["ADMINISTRATOR", "EDITOR"],
                label: "Créer",
                href: "/new",
                subButtons: []
            },
        ]
    },
    {
        roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
        label: "Bornes",
        href: "/chargepoint",
        subButtons: [
            {
                roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
                label: "Afficher/Modifier",
                href: "",
                subButtons: []
            },
            {
                roles: ["ADMINISTRATOR", "EDITOR"],
                label: "Créer",
                href: "/new",
                subButtons: []
            },
        ]
    },
    {
        roles: ["ADMINISTRATOR", "EDITOR"],
        label: "Firmware",
        href: "/firmware",
        subButtons: [
            {
                roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
                label: "Afficher/Modifier",
                href: "",
                subButtons: []
            },
            {
                roles: ["ADMINISTRATOR", "EDITOR"],
                label: "Créer",
                href: "/new",
                subButtons: []
            }
        ]
    },
    {
        roles: ["ADMINISTRATOR"],
        label: "Gestion des comptes",
        href: "/account",
        subButtons: [
            {
                roles: ["ADMINISTRATOR"],
                label: "Afficher/Modifier",
                href: "",
                subButtons: []
            },
            {
                roles: ["ADMINISTRATOR"],
                label: "Créer",
                href: "/new",
                subButtons: []
            }
        ]
    },
    {
        roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
        label: "Logs",
        href: "/logs",
        subButtons: [
            {
                roles: ["ADMINISTRATOR", "EDITOR", "VISUALIZER"],
                label: "Fonctionnel",
                href: "/business",
                subButtons: []
            },
            {
                roles: ["ADMINISTRATOR", "EDITOR"],
                label: "Technique",
                href: "/technical",
                subButtons: []
            }
        ]
    },
]