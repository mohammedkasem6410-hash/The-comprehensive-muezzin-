package com.example.model

enum class CalculationMethod(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0 // 0 means use ishaAngle, >0 means minutes after Maghrib
) {
    EGYPTIAN(
        id = "egyptian",
        arabicName = "الهيئة المصرية العامة للمساحة",
        englishName = "Egyptian General Authority of Survey",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        id = "umm_al_qura",
        arabicName = "جامعة أم القرى - مكة المكرمة",
        englishName = "Umm Al-Qura University, Makkah",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    MUSLIM_WORLD_LEAGUE(
        id = "mwl",
        arabicName = "رابطة العالم الإسلامي",
        englishName = "Muslim World League",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ISNA(
        id = "isna",
        arabicName = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        englishName = "Islamic Society of North America",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    KARACHI(
        id = "karachi",
        arabicName = "جامعة العلوم الإسلامية بكراتشي",
        englishName = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    TEHRAN(
        id = "tehran",
        arabicName = "معهد الجيوفيزياء بجامعة طهران",
        englishName = "Institute of Geophysics, Tehran",
        fajrAngle = 17.7,
        ishaAngle = 14.0
    ),
    GULF(
        id = "gulf",
        arabicName = "الهيئة العامة للشؤون الإسلامية والأوقاف (الخليج)",
        englishName = "Gulf Region / UAE Awqaf",
        fajrAngle = 19.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    TURKEY(
        id = "turkey",
        arabicName = "رئاسة الشؤون الدينية التركية (ديانت)",
        englishName = "Diyanet İşleri Başkanlığı, Turkey",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    SINGAPORE(
        id = "singapore",
        arabicName = "المجلس الإسلامي بسنغافورة (MUIS)",
        englishName = "Majlis Ugama Islam Singapura",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    );

    fun getName(isArabic: Boolean): String = if (isArabic) arabicName else englishName
}

enum class JuristicMethod(val arabicName: String, val englishName: String, val shadowFactor: Double) {
    SHAFI(arabicName = "الشافعي، المالكي، الحنبلي (المعياري)", englishName = "Standard (Shafi'i, Maliki, Hanbali)", shadowFactor = 1.0),
    HANAFI(arabicName = "الحنفي (ظل الضعف)", englishName = "Hanafi (Double Shadow)", shadowFactor = 2.0);

    fun getName(isArabic: Boolean): String = if (isArabic) arabicName else englishName
}
