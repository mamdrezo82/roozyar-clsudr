package com.roozyar.app.core.common

private const val PERSIAN_ZERO = '۰'

/** Converts ASCII digits to Persian digits, leaving every other character untouched. */
fun String.toPersianDigits(): String = buildString(length) {
    for (c in this@toPersianDigits) append(if (c in '0'..'9') PERSIAN_ZERO + (c - '0') else c)
}

fun Int.toPersianDigits(): String = toString().toPersianDigits()
