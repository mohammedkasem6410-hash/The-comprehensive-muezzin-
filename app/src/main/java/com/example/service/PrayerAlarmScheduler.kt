package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.calculator.PrayerTimeCalculator
import com.example.model.AppSettings
import com.example.model.Prayer
import com.example.model.SuhoorAlertMode
import com.example.receiver.PrayerAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"

    const val ACTION_PRAYER_ADHAN = "com.example.ACTION_PRAYER_ADHAN"
    const val ACTION_PRE_ALERT = "com.example.ACTION_PRE_ALERT"
    const val ACTION_SUHOOR_ALERT = "com.example.ACTION_SUHOOR_ALERT"

    const val EXTRA_PRAYER_KEY = "extra_prayer_key"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_REMAINING_MINUTES = "extra_remaining_minutes"

    fun scheduleAllAlarms(context: Context, settings: AppSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Check exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms, permission not granted")
            }
        }

        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        val todayTimes = PrayerTimeCalculator.calculate(
            date = today,
            latitude = settings.customLatitude,
            longitude = settings.customLongitude,
            cityNameAr = settings.customCityNameAr,
            cityNameEn = settings.customCityNameEn,
            method = settings.calculationMethod,
            juristicMethod = settings.juristicMethod,
            zoneId = zoneId
        )

        val tomorrowTimes = PrayerTimeCalculator.calculate(
            date = today.plusDays(1),
            latitude = settings.customLatitude,
            longitude = settings.customLongitude,
            cityNameAr = settings.customCityNameAr,
            cityNameEn = settings.customCityNameEn,
            method = settings.calculationMethod,
            juristicMethod = settings.juristicMethod,
            zoneId = zoneId
        )

        val now = LocalDateTime.now(zoneId)

        // Schedule Adhans and Pre-Alerts for today & tomorrow
        val relevantPrayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

        var requestCode = 1000

        listOf(todayTimes, tomorrowTimes).forEach { dayData ->
            relevantPrayers.forEach { prayer ->
                val entry = dayData.getEntry(prayer) ?: return@forEach
                val prayerDateTime = LocalDateTime.of(dayData.date, entry.time)

                // 1. Exact Adhan Alarm
                if (prayerDateTime.isAfter(now)) {
                    val epochMillis = prayerDateTime.atZone(zoneId).toInstant().toEpochMilli()
                    val adhanIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = ACTION_PRAYER_ADHAN
                        putExtra(EXTRA_PRAYER_KEY, prayer.key)
                        putExtra(EXTRA_PRAYER_NAME, prayer.getDisplayName(settings.isArabic, dayData.isFriday))
                    }
                    val pi = PendingIntent.getBroadcast(
                        context,
                        requestCode++,
                        adhanIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, epochMillis, pi)
                }

                // 2. Pre-Alert Alarm (e.g. 15 mins before)
                if (settings.preAlertEnabled) {
                    val preAlertDateTime = prayerDateTime.minusMinutes(settings.preAlertMinutes.toLong())
                    if (preAlertDateTime.isAfter(now)) {
                        val epochMillis = preAlertDateTime.atZone(zoneId).toInstant().toEpochMilli()
                        val preAlertIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                            action = ACTION_PRE_ALERT
                            putExtra(EXTRA_PRAYER_KEY, prayer.key)
                            putExtra(EXTRA_PRAYER_NAME, prayer.getDisplayName(settings.isArabic, dayData.isFriday))
                            putExtra(EXTRA_REMAINING_MINUTES, settings.preAlertMinutes)
                        }
                        val pi = PendingIntent.getBroadcast(
                            context,
                            requestCode++,
                            preAlertIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setExactAlarm(alarmManager, epochMillis, pi)
                    }
                }
            }
        }

        // 3. Suhoor / Musaharati Alarm
        if (settings.suhoorEnabled) {
            scheduleSuhoor(context, alarmManager, settings, todayTimes, tomorrowTimes, now, zoneId)
        }
    }

    private fun scheduleSuhoor(
        context: Context,
        alarmManager: AlarmManager,
        settings: AppSettings,
        todayTimes: com.example.model.PrayerTimesData,
        tomorrowTimes: com.example.model.PrayerTimesData,
        now: LocalDateTime,
        zoneId: ZoneId
    ) {
        val suhoorDateTime: LocalDateTime = if (settings.suhoorMode == SuhoorAlertMode.BEFORE_FAJR) {
            val fajrToday = todayTimes.getEntry(Prayer.FAJR)!!.time
            val fajrDateTimeToday = LocalDateTime.of(todayTimes.date, fajrToday)
            val suhoorToday = fajrDateTimeToday.minusMinutes(settings.suhoorMinutesBeforeFajr.toLong())

            if (suhoorToday.isAfter(now)) {
                suhoorToday
            } else {
                val fajrTomorrow = tomorrowTimes.getEntry(Prayer.FAJR)!!.time
                val fajrDateTimeTomorrow = LocalDateTime.of(tomorrowTimes.date, fajrTomorrow)
                fajrDateTimeTomorrow.minusMinutes(settings.suhoorMinutesBeforeFajr.toLong())
            }
        } else {
            val fixedToday = LocalDateTime.of(todayTimes.date, java.time.LocalTime.of(settings.suhoorFixedHour, settings.suhoorFixedMinute))
            if (fixedToday.isAfter(now)) {
                fixedToday
            } else {
                fixedToday.plusDays(1)
            }
        }

        val epochMillis = suhoorDateTime.atZone(zoneId).toInstant().toEpochMilli()
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_SUHOOR_ALERT
        }
        val pi = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(alarmManager, epochMillis, pi)
        Log.d(TAG, "Suhoor alert scheduled for: $suhoorDateTime")
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, operation: PendingIntent) {
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm", e)
        }
    }
}
