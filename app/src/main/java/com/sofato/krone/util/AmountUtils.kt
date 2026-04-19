package com.sofato.krone.util

import kotlin.math.pow
import kotlin.math.roundToLong

fun Long.toMajorUnits(decimalPlaces: Int): Double =
    this / 10.0.pow(decimalPlaces)

fun Double.toMinorUnits(decimalPlaces: Int): Long =
    (this * 10.0.pow(decimalPlaces)).roundToLong()

/**
 * Converts a minor-unit amount in one currency into the minor-unit equivalent
 * in another, accounting for each currency's decimal-place count. Applying the
 * rate directly to minor units is only correct when both currencies share the
 * same decimal count (e.g. EUR↔DKK); for pairs like JPY (0)↔DKK (2) it's off
 * by a factor of 10^(toDecimals-fromDecimals).
 */
fun convertMinor(
    amountMinor: Long,
    rate: Double,
    fromDecimals: Int,
    toDecimals: Int,
): Long = (amountMinor.toMajorUnits(fromDecimals) * rate).toMinorUnits(toDecimals)
