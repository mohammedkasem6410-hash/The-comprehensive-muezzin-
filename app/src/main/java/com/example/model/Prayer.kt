package com.example.model

enum class Prayer(val key: String) {
    FAJR("fajr"),
    SUNRISE("sunrise"),
    DHUHR("dhuhr"),
    ASR("asr"),
    MAGHRIB("maghrib"),
    ISHA("isha");

    fun getDisplayName(isArabic: Boolean, isFriday: Boolean = false): String {
        return if (isArabic) {
            when (this) {
                FAJR -> "الفجر"
                SUNRISE -> "الشروق"
                DHUHR -> if (isFriday) "الجمعة" else "الظهر"
                ASR -> "العصر"
                MAGHRIB -> "المغرب"
                ISHA -> "العشاء"
            }
        } else {
            when (this) {
                FAJR -> "Fajr"
                SUNRISE -> "Sunrise"
                DHUHR -> if (isFriday) "Jumu'ah" else "Dhuhr"
                ASR -> "Asr"
                MAGHRIB -> "Maghrib"
                ISHA -> "Isha"
            }
        }
    }
}
