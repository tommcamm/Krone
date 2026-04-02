package com.sofato.krone.util

import kotlin.math.pow
import kotlin.math.roundToLong

fun Long.toMajorUnits(decimalPlaces: Int): Double =
    this / 10.0.pow(decimalPlaces)

fun Double.toMinorUnits(decimalPlaces: Int): Long =
    (this * 10.0.pow(decimalPlaces)).roundToLong()
