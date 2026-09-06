package com.example.calculator

import com.example.model.CalculationMethod
import com.example.model.JuristicMethod
import com.example.model.Prayer
import com.example.model.PrayerTimeEntry
import com.example.model.PrayerTimesData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

object PrayerTimeCalculator {

    /**
     * Calculates complete prayer times for a given date, coordinates, calculation method, and juristic method.
     */
    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        cityNameAr: String,
        cityNameEn: String,
        method: CalculationMethod,
        juristicMethod: JuristicMethod,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): PrayerTimesData {
        val timezoneOffsetHours = zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0
        val julianDay = getJulianDay(date.year, date.monthValue, date.dayOfMonth)

        // Sun astronomical position
        val d = julianDay - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqOfTime = q / 15.0 - fixHour(ra)

        // Solar Noon in local standard hours
        val solarNoon = fixHour(12.0 + timezoneOffsetHours - (longitude / 15.0) - eqOfTime)

        // Sunrise and Sunset (-0.8333 degrees altitude for refraction & sun semidiameter)
        val sunAlt = -0.8333
        val sunriseHourAngle = calculateHourAngle(latitude, declination, sunAlt)
        val sunrise = if (sunriseHourAngle.isNaN()) solarNoon - 6.0 else solarNoon - (sunriseHourAngle / 15.0)
        val sunset = if (sunriseHourAngle.isNaN()) solarNoon + 6.0 else solarNoon + (sunriseHourAngle / 15.0)

        // Fajr
        val fajrAngle = -method.fajrAngle
        val fajrHourAngle = calculateHourAngle(latitude, declination, fajrAngle)
        val fajr = if (fajrHourAngle.isNaN()) sunrise - 1.5 else solarNoon - (fajrHourAngle / 15.0)

        // Dhuhr: Solar noon + 1.5 minute safety buffer
        val dhuhr = solarNoon + (1.5 / 60.0)

        // Asr
        val shadowFactor = juristicMethod.shadowFactor
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val delta = abs(latitude - declination)
        val asrAlt = Math.toDegrees(atan(1.0 / (shadowFactor + tan(Math.toRadians(delta)))))
        val asrHourAngle = calculateHourAngle(latitude, declination, asrAlt)
        val asr = if (asrHourAngle.isNaN()) solarNoon + 3.0 else solarNoon + (asrHourAngle / 15.0)

        // Maghrib
        val maghrib = sunset + (1.0 / 60.0) // 1 min buffer after sunset

        // Isha
        val isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaAngle = -method.ishaAngle
            val ishaHourAngle = calculateHourAngle(latitude, declination, ishaAngle)
            if (ishaHourAngle.isNaN()) sunset + 1.5 else solarNoon + (ishaHourAngle / 15.0)
        }

        // Convert decimal hours to LocalTimes
        val fajrTime = decimalHoursToLocalTime(fajr)
        val sunriseTime = decimalHoursToLocalTime(sunrise)
        val dhuhrTime = decimalHoursToLocalTime(dhuhr)
        val asrTime = decimalHoursToLocalTime(asr)
        val maghribTime = decimalHoursToLocalTime(maghrib)
        val ishaTime = decimalHoursToLocalTime(isha)

        val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        val entries = listOf(
            PrayerTimeEntry(Prayer.FAJR, fajrTime, fajrTime.format(timeFormatter)),
            PrayerTimeEntry(Prayer.SUNRISE, sunriseTime, sunriseTime.format(timeFormatter)),
            PrayerTimeEntry(Prayer.DHUHR, dhuhrTime, dhuhrTime.format(timeFormatter)),
            PrayerTimeEntry(Prayer.ASR, asrTime, asrTime.format(timeFormatter)),
            PrayerTimeEntry(Prayer.MAGHRIB, maghribTime, maghribTime.format(timeFormatter)),
            PrayerTimeEntry(Prayer.ISHA, ishaTime, ishaTime.format(timeFormatter))
        )

        val now = LocalTime.now(zoneId)
        val currentAndNext = determineCurrentAndNextPrayer(now, entries)
        val nextPrayer = currentAndNext.second
        val currentPrayer = currentAndNext.first

        val nextPrayerTime = entries.find { it.prayer == nextPrayer }?.time ?: fajrTime

        val nowDateTime = LocalDateTime.of(date, now)
        var targetDateTime = LocalDateTime.of(date, nextPrayerTime)
        if (targetDateTime.isBefore(nowDateTime)) {
            // Next prayer is tomorrow (e.g. Fajr after Isha)
            targetDateTime = targetDateTime.plusDays(1)
        }

        val millisUntilNext = java.time.Duration.between(nowDateTime, targetDateTime).toMillis().coerceAtLeast(0L)
        val hours = millisUntilNext / (1000 * 3600)
        val minutes = (millisUntilNext / (1000 * 60)) % 60
        val seconds = (millisUntilNext / 1000) % 60
        val formattedRemaining = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)

        val hijri = HijriCalendarHelper.getHijriDate(date)

        val gregorianFormatterAr = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
        val gregorianFormatterEn = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

        return PrayerTimesData(
            date = date,
            cityName = cityNameEn,
            cityNameAr = cityNameAr,
            latitude = latitude,
            longitude = longitude,
            method = method,
            juristicMethod = juristicMethod,
            isFriday = isFriday,
            entries = entries,
            hijriDateFormattedAr = hijri.formattedAr,
            hijriDateFormattedEn = hijri.formattedEn,
            gregorianDateFormattedAr = date.format(gregorianFormatterAr),
            gregorianDateFormattedEn = date.format(gregorianFormatterEn),
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            nextPrayerTime = nextPrayerTime,
            millisUntilNextPrayer = millisUntilNext,
            formattedRemainingCountdown = formattedRemaining
        )
    }

    private fun determineCurrentAndNextPrayer(
        now: LocalTime,
        entries: List<PrayerTimeEntry>
    ): Pair<Prayer?, Prayer> {
        val fajr = entries.find { it.prayer == Prayer.FAJR }!!.time
        val sunrise = entries.find { it.prayer == Prayer.SUNRISE }!!.time
        val dhuhr = entries.find { it.prayer == Prayer.DHUHR }!!.time
        val asr = entries.find { it.prayer == Prayer.ASR }!!.time
        val maghrib = entries.find { it.prayer == Prayer.MAGHRIB }!!.time
        val isha = entries.find { it.prayer == Prayer.ISHA }!!.time

        return when {
            now.isBefore(fajr) -> Pair(null, Prayer.FAJR)
            now.isBefore(sunrise) -> Pair(Prayer.FAJR, Prayer.SUNRISE)
            now.isBefore(dhuhr) -> Pair(Prayer.SUNRISE, Prayer.DHUHR)
            now.isBefore(asr) -> Pair(Prayer.DHUHR, Prayer.ASR)
            now.isBefore(maghrib) -> Pair(Prayer.ASR, Prayer.MAGHRIB)
            now.isBefore(isha) -> Pair(Prayer.MAGHRIB, Prayer.ISHA)
            else -> Pair(Prayer.ISHA, Prayer.FAJR) // After Isha, next prayer is Fajr tomorrow
        }
    }

    private fun calculateHourAngle(lat: Double, dec: Double, alt: Double): Double {
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val altRad = Math.toRadians(alt)
        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        return if (cosH < -1.0 || cosH > 1.0) Double.NaN else Math.toDegrees(acos(cosH))
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }

    private fun decimalHoursToLocalTime(decimalHours: Double): LocalTime {
        val fixed = fixHour(decimalHours)
        val hour = fixed.toInt().coerceIn(0, 23)
        val minuteFraction = (fixed - hour) * 60.0
        val minute = minuteFraction.toInt().coerceIn(0, 59)
        val second = ((minuteFraction - minute) * 60.0).toInt().coerceIn(0, 59)
        return LocalTime.of(hour, minute, second)
    }
}
