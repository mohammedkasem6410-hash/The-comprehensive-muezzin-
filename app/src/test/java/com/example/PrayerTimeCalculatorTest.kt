package com.example

import com.example.calculator.HijriCalendarHelper
import com.example.calculator.PrayerTimeCalculator
import com.example.model.CalculationMethod
import com.example.model.JuristicMethod
import com.example.model.Prayer
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerTimeCalculatorTest {

    @Test
    fun testPrayerCalculationReturnsValidTimes() {
        val date = LocalDate.of(2026, 9, 6)
        val prayerData = PrayerTimeCalculator.calculate(
            date = date,
            latitude = 30.0626,
            longitude = 31.2497,
            cityNameAr = "الفجالة، القاهرة",
            cityNameEn = "Al-Fajjala, Cairo",
            method = CalculationMethod.EGYPTIAN,
            juristicMethod = JuristicMethod.SHAFI,
            zoneId = ZoneId.of("Africa/Cairo")
        )

        assertNotNull(prayerData)
        assertEquals(6, prayerData.entries.size)

        val fajr = prayerData.getEntry(Prayer.FAJR)!!.time
        val sunrise = prayerData.getEntry(Prayer.SUNRISE)!!.time
        val dhuhr = prayerData.getEntry(Prayer.DHUHR)!!.time
        val asr = prayerData.getEntry(Prayer.ASR)!!.time
        val maghrib = prayerData.getEntry(Prayer.MAGHRIB)!!.time
        val isha = prayerData.getEntry(Prayer.ISHA)!!.time

        // Chronological order verification
        assertTrue("Fajr must be before sunrise", fajr.isBefore(sunrise))
        assertTrue("Sunrise must be before dhuhr", sunrise.isBefore(dhuhr))
        assertTrue("Dhuhr must be before asr", dhuhr.isBefore(asr))
        assertTrue("Asr must be before maghrib", asr.isBefore(maghrib))
        assertTrue("Maghrib must be before isha", maghrib.isBefore(isha))

        assertNotNull(prayerData.nextPrayer)
        assertTrue(prayerData.formattedRemainingCountdown.isNotEmpty())
    }

    @Test
    fun testFridayJumuahConversion() {
        // Find a Friday
        var date = LocalDate.now()
        while (date.dayOfWeek != java.time.DayOfWeek.FRIDAY) {
            date = date.plusDays(1)
        }

        val prayerData = PrayerTimeCalculator.calculate(
            date = date,
            latitude = 30.0626,
            longitude = 31.2497,
            cityNameAr = "القاهرة",
            cityNameEn = "Cairo",
            method = CalculationMethod.EGYPTIAN,
            juristicMethod = JuristicMethod.SHAFI
        )

        assertTrue(prayerData.isFriday)
        val dhuhrName = Prayer.DHUHR.getDisplayName(isArabic = true, isFriday = prayerData.isFriday)
        assertEquals("الجمعة", dhuhrName)
    }

    @Test
    fun testHijriDateGeneration() {
        val date = LocalDate.of(2026, 3, 20)
        val hijri = HijriCalendarHelper.getHijriDate(date)
        assertNotNull(hijri)
        assertTrue(hijri.formattedAr.isNotEmpty())
        assertTrue(hijri.formattedEn.isNotEmpty())
        assertTrue(hijri.year > 1400)
    }
}
