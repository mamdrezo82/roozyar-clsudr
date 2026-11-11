package com.roozyar.app.core.date

/**
 * Arithmetic Gregorian <-> Jalali conversion (33-year-cycle algorithm with a break table,
 * valid for Jalali years -61 until 3177). Pure Kotlin so it can be unit-tested on the JVM.
 * Integer division here deliberately truncates toward zero, as the reference algorithm does.
 */
internal object JalaliCalendarMath {
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    const val MIN_YEAR = -61
    const val MAX_YEAR_EXCLUSIVE = 3178

    data class YearInfo(val leap: Int, val gregorianYear: Int, val marchDay: Int)
    data class Ymd(val year: Int, val month: Int, val day: Int)

    private fun yearInfo(jy: Int): YearInfo {
        require(jy >= breaks.first() && jy < breaks.last()) { "Invalid Jalali year $jy" }
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + ((jump % 33) / 4)
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + (((n % 33) + 3) / 4)
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return YearInfo(leap, gy, march)
    }

    /** True when [jy] has 30 days in Esfand. */
    fun isLeap(jy: Int): Boolean = yearInfo(jy).leap == 0

    private fun gregorianToDayNumber(gy: Int, gm: Int, gd: Int): Int {
        var d = ((gy + (gm - 8) / 6 + 100100) * 1461) / 4 + (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
        d = d - (((gy + 100100 + (gm - 8) / 6) / 100) * 3) / 4 + 752
        return d
    }

    private fun dayNumberToGregorian(jdn: Int): Ymd {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3 / 4) * 4 - 3908
        val i = ((j % 1461) / 4) * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return Ymd(gy, gm, gd)
    }

    private fun jalaliToDayNumber(jy: Int, jm: Int, jd: Int): Int {
        val info = yearInfo(jy)
        return gregorianToDayNumber(info.gregorianYear, 3, info.marchDay) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    private fun dayNumberToJalali(jdn: Int): Ymd {
        val gy = dayNumberToGregorian(jdn).year
        var jy = gy - 621
        val info = yearInfo(jy)
        var k = jdn - gregorianToDayNumber(gy, 3, info.marchDay)
        if (k >= 0) {
            if (k <= 185) return Ymd(jy, 1 + k / 31, k % 31 + 1)
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (info.leap == 1) k += 1
        }
        return Ymd(jy, 7 + k / 30, k % 30 + 1)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Ymd = dayNumberToJalali(gregorianToDayNumber(gy, gm, gd))

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Ymd = dayNumberToGregorian(jalaliToDayNumber(jy, jm, jd))
}
