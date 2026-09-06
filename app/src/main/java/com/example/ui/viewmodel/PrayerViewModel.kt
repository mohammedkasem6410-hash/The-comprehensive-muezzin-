package com.example.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.PrayerTimeCalculator
import com.example.model.*
import com.example.repository.SettingsRepository
import com.example.service.PrayerAlarmScheduler
import com.example.service.PrayerForegroundService
import com.example.widget.PrayerAppWidgetProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)

    private val _settings = MutableStateFlow(settingsRepo.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _prayerData = MutableStateFlow(calculateCurrentTimes(_settings.value))
    val prayerData: StateFlow<PrayerTimesData> = _prayerData.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Start foreground service if enabled
        if (_settings.value.persistentNotificationEnabled) {
            PrayerForegroundService.start(application)
        }

        // Schedule alarms
        PrayerAlarmScheduler.scheduleAllAlarms(application, _settings.value)

        // Live ticking loop for countdown
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _prayerData.value = calculateCurrentTimes(_settings.value)
            }
        }
    }

    private fun calculateCurrentTimes(currentSettings: AppSettings): PrayerTimesData {
        return PrayerTimeCalculator.calculate(
            date = LocalDate.now(),
            latitude = currentSettings.customLatitude,
            longitude = currentSettings.customLongitude,
            cityNameAr = currentSettings.customCityNameAr,
            cityNameEn = currentSettings.customCityNameEn,
            method = currentSettings.calculationMethod,
            juristicMethod = currentSettings.juristicMethod,
            zoneId = ZoneId.systemDefault()
        )
    }

    fun setCalculationMethod(method: CalculationMethod) {
        val updated = _settings.value.copy(calculationMethod = method)
        saveAndApply(updated)
    }

    fun setJuristicMethod(juristicMethod: JuristicMethod) {
        val updated = _settings.value.copy(juristicMethod = juristicMethod)
        saveAndApply(updated)
    }

    fun selectPresetCity(city: CityLocation) {
        val index = PRESET_CITIES.indexOf(city).coerceAtLeast(0)
        val updated = _settings.value.copy(
            locationMode = LocationMode.MANUAL,
            selectedCityIndex = index,
            customLatitude = city.latitude,
            customLongitude = city.longitude,
            customCityNameAr = city.nameAr,
            customCityNameEn = city.nameEn,
            calculationMethod = city.defaultMethod
        )
        saveAndApply(updated)
    }

    fun toggleLanguage() {
        val newLang = !_settings.value.isArabic
        val updated = _settings.value.copy(isArabic = newLang)
        saveAndApply(updated)
    }

    fun setPreAlert(enabled: Boolean, minutes: Int) {
        val updated = _settings.value.copy(preAlertEnabled = enabled, preAlertMinutes = minutes)
        saveAndApply(updated)
    }

    fun setSuhoorSettings(
        enabled: Boolean,
        mode: SuhoorAlertMode,
        minutesBeforeFajr: Int,
        fixedHour: Int,
        fixedMinute: Int,
        mediaUri: String?
    ) {
        val updated = _settings.value.copy(
            suhoorEnabled = enabled,
            suhoorMode = mode,
            suhoorMinutesBeforeFajr = minutesBeforeFajr,
            suhoorFixedHour = fixedHour,
            suhoorFixedMinute = fixedMinute,
            suhoorMediaUri = mediaUri
        )
        saveAndApply(updated)
    }

    fun setAdhanVideoUri(uriString: String?) {
        val updated = _settings.value.copy(adhanVideoUri = uriString)
        saveAndApply(updated)
    }

    fun togglePersistentNotification(enabled: Boolean) {
        val updated = _settings.value.copy(persistentNotificationEnabled = enabled)
        saveAndApply(updated)
        if (enabled) {
            PrayerForegroundService.start(getApplication())
        } else {
            PrayerForegroundService.stop(getApplication())
        }
    }

    fun requestGpsLocation() {
        val context = getApplication<Application>()
        _isLocating.value = true
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    _isLocating.value = false
                    if (location != null) {
                        val lat = location.latitude
                        val lng = location.longitude
                        val updated = _settings.value.copy(
                            locationMode = LocationMode.AUTO_GPS,
                            customLatitude = lat,
                            customLongitude = lng,
                            customCityNameAr = "الموقع الحالي (GPS)",
                            customCityNameEn = "Current Location (GPS)"
                        )
                        saveAndApply(updated)
                        _message.value = if (updated.isArabic) "تم تحديث الموقع عبر GPS بنجاح" else "GPS Location updated successfully"
                    } else {
                        _message.value = if (_settings.value.isArabic) "تعذر الحصول على إحداثيات GPS، يرجى تفعيل الموقع" else "Could not get GPS coordinates"
                    }
                }
                .addOnFailureListener {
                    _isLocating.value = false
                    _message.value = if (_settings.value.isArabic) "فشل تحديد الموقع" else "Location lookup failed"
                }
        } catch (e: SecurityException) {
            _isLocating.value = false
            _message.value = if (_settings.value.isArabic) "يرجى منح إذن الوصول للموقع" else "Please grant location permission"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun saveAndApply(updatedSettings: AppSettings) {
        settingsRepo.updateSettings(updatedSettings)
        _settings.value = updatedSettings
        _prayerData.value = calculateCurrentTimes(updatedSettings)
        PrayerAlarmScheduler.scheduleAllAlarms(getApplication(), updatedSettings)
        PrayerAppWidgetProvider.updateAllWidgets(getApplication())
    }
}
