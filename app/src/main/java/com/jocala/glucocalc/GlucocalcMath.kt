package com.jocala.glucocalc

/**
 * ADAG math, matching the SwiftUI app (Calculator.swift).
 * Do not "improve" the constants.
 */
object GlucocalcMath {

    data class EAG(val mgdl: Double, val mmolL: Double)

    data class A1c(val ngsp: Double, val ifcc: Double)

    // eAG from input HbA1c. mmol: input is IFCC mmol/mol, else NGSP %.
    fun eAG(fromA1c: Double, mmol: Boolean): EAG {
        var a1c = fromA1c
        if (mmol) a1c = (0.09148 * a1c) + 2.152
        val mgdl = (28.7 * a1c) - 46.7
        return EAG(mgdl, mgdl / 18)
    }

    // HbA1c from input eAG. mmol: input is mmol/L, else mg/dl.
    fun a1c(fromEAG: Double, mmol: Boolean): A1c {
        var eag = fromEAG
        if (mmol) eag = eag * 18
        val ngsp = (eag + 46.7) / 28.7
        return A1c(ngsp, (10.93 * ngsp) - 23.50)
    }
}
