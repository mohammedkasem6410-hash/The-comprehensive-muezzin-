package com.example.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class PrayerTimeEntry(
    val prayer: Prayer,
    val time: LocalTime,
    val timeFormatted: String
)

data class PrayerTimesData(
    val date: LocalDate,
    val cityName: String,
    val cityNameAr: String,
    val latitude: Double,
    val longitude: Double,
    val method: CalculationMethod,
    val juristicMethod: JuristicMethod,
    val isFriday: Boolean,
    val entries: List<PrayerTimeEntry>,
    val hijriDateFormattedAr: String,
    val hijriDateFormattedEn: String,
    val gregorianDateFormattedAr: String,
    val gregorianDateFormattedEn: String,
    val currentPrayer: Prayer?,
    val nextPrayer: Prayer,
    val nextPrayerTime: LocalTime,
    val millisUntilNextPrayer: Long,
    val formattedRemainingCountdown: String // e.g. "01:24:15"
) {
    fun getEntry(prayer: Prayer): PrayerTimeEntry? = entries.find { it.prayer == prayer }
}

data class CityLocation(
    val nameEn: String,
    val nameAr: String,
    val latitude: Double,
    val longitude: Double,
    val countryEn: String,
    val countryAr: String,
    val defaultMethod: CalculationMethod
)

val PRESET_CITIES = listOf(
    CityLocation("Al-Fajjala, Cairo", "الفجالة، القاهرة", 30.0626, 31.2497, "Egypt", "مصر", CalculationMethod.EGYPTIAN),
    CityLocation("Cairo", "القاهرة", 30.0444, 31.2357, "Egypt", "مصر", CalculationMethod.EGYPTIAN),
    CityLocation("Alexandria", "الإسكندرية", 31.2001, 29.9187, "Egypt", "مصر", CalculationMethod.EGYPTIAN),
    CityLocation("Makkah", "مكة المكرمة", 21.4225, 39.8262, "Saudi Arabia", "المملكة العربية السعودية", CalculationMethod.UMM_AL_QURA),
    CityLocation("Madinah", "المدينة المنورة", 24.4672, 39.6111, "Saudi Arabia", "المملكة العربية السعودية", CalculationMethod.UMM_AL_QURA),
    CityLocation("Riyadh", "الرياض", 24.7136, 46.6753, "Saudi Arabia", "المملكة العربية السعودية", CalculationMethod.UMM_AL_QURA),
    CityLocation("Jerusalem", "القدس الشريف", 31.7683, 35.2137, "Palestine", "فلسطين", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    CityLocation("Dubai", "دبي", 25.2048, 55.2708, "United Arab Emirates", "الإمارات العربية المتحدة", CalculationMethod.GULF),
    CityLocation("Istanbul", "إسطنبول", 41.0082, 28.9784, "Turkey", "تركيا", CalculationMethod.TURKEY),
    CityLocation("London", "لندن", 51.5074, -0.1278, "United Kingdom", "المملكة المتحدة", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    CityLocation("New York", "نيويورك", 40.7128, -74.0060, "United States", "الولايات المتحدة", CalculationMethod.ISNA),
    CityLocation("Karachi", "كراتشي", 24.8607, 67.0011, "Pakistan", "باكستان", CalculationMethod.KARACHI),
    CityLocation("Jakarta", "جاكرتا", -6.2088, 106.8456, "Indonesia", "إندونيسيا", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    CityLocation("Singapore", "سنغافورة", 1.3521, 103.8198, "Singapore", "سنغافورة", CalculationMethod.SINGAPORE)
)
