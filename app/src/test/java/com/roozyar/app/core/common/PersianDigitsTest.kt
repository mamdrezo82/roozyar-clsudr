package com.roozyar.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PersianDigitsTest {
    @Test
    fun convertsAllDigits() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
    }

    @Test
    fun leavesOtherCharactersAlone() {
        assertEquals("۱۴۰۵/۰۸/۲۰ abc آبان", "1405/08/20 abc آبان".toPersianDigits())
    }

    @Test
    fun convertsInts() {
        assertEquals("۱۲۰۰", 1200.toPersianDigits())
    }
}
