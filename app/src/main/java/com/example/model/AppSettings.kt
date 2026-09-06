package com.example.model

enum class LocationMode {
    AUTO_GPS,
    MANUAL
}

enum class SuhoorAlertMode {
    BEFORE_FAJR,
    FIXED_TIME
}

data class AppSettings(
    val isArabic: Boolean = true,
    val locationMode: LocationMode = LocationMode.MANUAL,
    val selectedCityIndex: Int = 0, // Al-Fajjala, Cairo default
    val customLatitude: Double = 30.0626,
    val customLongitude: Double = 31.2497,
    val customCityNameAr: String = "الفجالة، القاهرة",
    val customCityNameEn: String = "Al-Fajjala, Cairo",
    val calculationMethod: CalculationMethod = CalculationMethod.EGYPTIAN,
    val juristicMethod: JuristicMethod = JuristicMethod.SHAFI,
    val preAlertEnabled: Boolean = true,
    val preAlertMinutes: Int = 15, // Minutes before prayer
    val preAlertRingtoneUri: String? = null,
    val adhanVideoUri: String? = null, // Custom video or null for default
    val adhanAudioUri: String? = null,
    // Musaharati (Suhoor) Settings
    val suhoorEnabled: Boolean = true,
    val suhoorMode: SuhoorAlertMode = SuhoorAlertMode.BEFORE_FAJR,
    val suhoorMinutesBeforeFajr: Int = 45,
    val suhoorFixedHour: Int = 3,
    val suhoorFixedMinute: Int = 30,
    val suhoorMediaUri: String? = null, // Custom audio/video or built-in
    // Persistent Notification
    val persistentNotificationEnabled: Boolean = true
)
