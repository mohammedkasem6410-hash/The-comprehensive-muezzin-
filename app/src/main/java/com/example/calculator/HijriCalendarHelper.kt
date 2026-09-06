package com.example.calculator

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class HijriDateInfo(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameAr: String,
    val monthNameEn: String,
    val dayNameAr: String,
    val dayNameEn: String,
    val formattedAr: String,
    val formattedEn: String
)

object HijriCalendarHelper {

    private val ARABIC_MONTHS = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val ENGLISH_MONTHS = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val ARABIC_DAYS = listOf(
        "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد"
    )

    private val ENGLISH_DAYS = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )

    fun getHijriDate(date: LocalDate): HijriDateInfo {
        val hijrahDate = HijrahDate.from(date)
        val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
        val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
        val hYear = hijrahDate.get(ChronoField.YEAR)

        val monthIndex = (hMonth - 1).coerceIn(0, 11)
        val dayOfWeekIndex = (date.dayOfWeek.value - 1).coerceIn(0, 6)

        val monthAr = ARABIC_MONTHS[monthIndex]
        val monthEn = ENGLISH_MONTHS[monthIndex]
        val dayAr = ARABIC_DAYS[dayOfWeekIndex]
        val dayEn = ENGLISH_DAYS[dayOfWeekIndex]

        val formattedAr = "$dayAr، $hDay $monthAr $hYear هـ"
        val formattedEn = "$dayEn, $hDay $monthEn $hYear AH"

        return HijriDateInfo(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameAr = monthAr,
            monthNameEn = monthEn,
            dayNameAr = dayAr,
            dayNameEn = dayEn,
            formattedAr = formattedAr,
            formattedEn = formattedEn
        )
    }
}
