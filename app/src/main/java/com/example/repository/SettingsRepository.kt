package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.CalculationMethod
import com.example.model.JuristicMethod
import com.example.model.LocationMode
import com.example.model.SuhoorAlertMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun loadSettings(): AppSettings {
        val isArabic = prefs.getBoolean(KEY_IS_ARABIC, true)
        val locationModeStr = prefs.getString(KEY_LOCATION_MODE, LocationMode.MANUAL.name) ?: LocationMode.MANUAL.name
        val locationMode = try { LocationMode.valueOf(locationModeStr) } catch (e: Exception) { LocationMode.MANUAL }
        val cityIndex = prefs.getInt(KEY_SELECTED_CITY_INDEX, 0)
        val lat = prefs.getFloat(KEY_LATITUDE, 30.0626f).toDouble()
        val lng = prefs.getFloat(KEY_LONGITUDE, 31.2497f).toDouble()
        val cityNameAr = prefs.getString(KEY_CITY_NAME_AR, "الفجالة، القاهرة") ?: "الفجالة، القاهرة"
        val cityNameEn = prefs.getString(KEY_CITY_NAME_EN, "Al-Fajjala, Cairo") ?: "Al-Fajjala, Cairo"

        val methodId = prefs.getString(KEY_METHOD, CalculationMethod.EGYPTIAN.id) ?: CalculationMethod.EGYPTIAN.id
        val method = CalculationMethod.entries.find { it.id == methodId } ?: CalculationMethod.EGYPTIAN

        val juristicStr = prefs.getString(KEY_JURISTIC, JuristicMethod.SHAFI.name) ?: JuristicMethod.SHAFI.name
        val juristic = try { JuristicMethod.valueOf(juristicStr) } catch (e: Exception) { JuristicMethod.SHAFI }

        val preAlertEnabled = prefs.getBoolean(KEY_PRE_ALERT_ENABLED, true)
        val preAlertMinutes = prefs.getInt(KEY_PRE_ALERT_MINUTES, 15)
        val preAlertTone = prefs.getString(KEY_PRE_ALERT_TONE, null)

        val adhanVideoUri = prefs.getString(KEY_ADHAN_VIDEO_URI, null)
        val adhanAudioUri = prefs.getString(KEY_ADHAN_AUDIO_URI, null)

        val suhoorEnabled = prefs.getBoolean(KEY_SUHOOR_ENABLED, true)
        val suhoorModeStr = prefs.getString(KEY_SUHOOR_MODE, SuhoorAlertMode.BEFORE_FAJR.name) ?: SuhoorAlertMode.BEFORE_FAJR.name
        val suhoorMode = try { SuhoorAlertMode.valueOf(suhoorModeStr) } catch (e: Exception) { SuhoorAlertMode.BEFORE_FAJR }
        val suhoorMinutes = prefs.getInt(KEY_SUHOOR_MINUTES, 45)
        val suhoorHour = prefs.getInt(KEY_SUHOOR_HOUR, 3)
        val suhoorMinute = prefs.getInt(KEY_SUHOOR_MINUTE, 30)
        val suhoorMediaUri = prefs.getString(KEY_SUHOOR_MEDIA_URI, null)

        val persistentNotificationEnabled = prefs.getBoolean(KEY_PERSISTENT_NOTIFICATION, true)

        return AppSettings(
            isArabic = isArabic,
            locationMode = locationMode,
            selectedCityIndex = cityIndex,
            customLatitude = lat,
            customLongitude = lng,
            customCityNameAr = cityNameAr,
            customCityNameEn = cityNameEn,
            calculationMethod = method,
            juristicMethod = juristic,
            preAlertEnabled = preAlertEnabled,
            preAlertMinutes = preAlertMinutes,
            preAlertRingtoneUri = preAlertTone,
            adhanVideoUri = adhanVideoUri,
            adhanAudioUri = adhanAudioUri,
            suhoorEnabled = suhoorEnabled,
            suhoorMode = suhoorMode,
            suhoorMinutesBeforeFajr = suhoorMinutes,
            suhoorFixedHour = suhoorHour,
            suhoorFixedMinute = suhoorMinute,
            suhoorMediaUri = suhoorMediaUri,
            persistentNotificationEnabled = persistentNotificationEnabled
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_IS_ARABIC, newSettings.isArabic)
            putString(KEY_LOCATION_MODE, newSettings.locationMode.name)
            putInt(KEY_SELECTED_CITY_INDEX, newSettings.selectedCityIndex)
            putFloat(KEY_LATITUDE, newSettings.customLatitude.toFloat())
            putFloat(KEY_LONGITUDE, newSettings.customLongitude.toFloat())
            putString(KEY_CITY_NAME_AR, newSettings.customCityNameAr)
            putString(KEY_CITY_NAME_EN, newSettings.customCityNameEn)
            putString(KEY_METHOD, newSettings.calculationMethod.id)
            putString(KEY_JURISTIC, newSettings.juristicMethod.name)
            putBoolean(KEY_PRE_ALERT_ENABLED, newSettings.preAlertEnabled)
            putInt(KEY_PRE_ALERT_MINUTES, newSettings.preAlertMinutes)
            putString(KEY_PRE_ALERT_TONE, newSettings.preAlertRingtoneUri)
            putString(KEY_ADHAN_VIDEO_URI, newSettings.adhanVideoUri)
            putString(KEY_ADHAN_AUDIO_URI, newSettings.adhanAudioUri)
            putBoolean(KEY_SUHOOR_ENABLED, newSettings.suhoorEnabled)
            putString(KEY_SUHOOR_MODE, newSettings.suhoorMode.name)
            putInt(KEY_SUHOOR_MINUTES, newSettings.suhoorMinutesBeforeFajr)
            putInt(KEY_SUHOOR_HOUR, newSettings.suhoorFixedHour)
            putInt(KEY_SUHOOR_MINUTE, newSettings.suhoorFixedMinute)
            putString(KEY_SUHOOR_MEDIA_URI, newSettings.suhoorMediaUri)
            putBoolean(KEY_PERSISTENT_NOTIFICATION, newSettings.persistentNotificationEnabled)
            apply()
        }
        _settings.value = newSettings
    }

    companion object {
        private const val KEY_IS_ARABIC = "is_arabic"
        private const val KEY_LOCATION_MODE = "location_mode"
        private const val KEY_SELECTED_CITY_INDEX = "selected_city_index"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_CITY_NAME_AR = "city_name_ar"
        private const val KEY_CITY_NAME_EN = "city_name_en"
        private const val KEY_METHOD = "method"
        private const val KEY_JURISTIC = "juristic"
        private const val KEY_PRE_ALERT_ENABLED = "pre_alert_enabled"
        private const val KEY_PRE_ALERT_MINUTES = "pre_alert_minutes"
        private const val KEY_PRE_ALERT_TONE = "pre_alert_tone"
        private const val KEY_ADHAN_VIDEO_URI = "adhan_video_uri"
        private const val KEY_ADHAN_AUDIO_URI = "adhan_audio_uri"
        private const val KEY_SUHOOR_ENABLED = "suhoor_enabled"
        private const val KEY_SUHOOR_MODE = "suhoor_mode"
        private const val KEY_SUHOOR_MINUTES = "suhoor_minutes"
        private const val KEY_SUHOOR_HOUR = "suhoor_hour"
        private const val KEY_SUHOOR_MINUTE = "suhoor_minute"
        private const val KEY_SUHOOR_MEDIA_URI = "suhoor_media_uri"
        private const val KEY_PERSISTENT_NOTIFICATION = "persistent_notification"
    }
}
