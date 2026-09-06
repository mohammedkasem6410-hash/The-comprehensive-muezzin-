package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calculator.PrayerTimeCalculator
import com.example.model.Prayer
import com.example.repository.SettingsRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, PrayerAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val settingsRepo = SettingsRepository(context)
            val settings = settingsRepo.loadSettings()
            val zoneId = ZoneId.systemDefault()

            val prayerData = PrayerTimeCalculator.calculate(
                date = LocalDate.now(zoneId),
                latitude = settings.customLatitude,
                longitude = settings.customLongitude,
                cityNameAr = settings.customCityNameAr,
                cityNameEn = settings.customCityNameEn,
                method = settings.calculationMethod,
                juristicMethod = settings.juristicMethod,
                zoneId = zoneId
            )

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)

            // 1. Top Bar: City Name (e.g. الفجالة)
            val fullCity = if (settings.isArabic) prayerData.cityNameAr else prayerData.cityName
            val cityDisplay = fullCity.split("،", ",").first().trim()
            views.setTextViewText(R.id.widget_city_name, cityDisplay)

            // 2. Main Blue Header: Active or Next Prayer & Prominent Countdown (+ 47:45)
            val headerPrayer = prayerData.currentPrayer ?: prayerData.nextPrayer
            val headerTitle = headerPrayer.getDisplayName(settings.isArabic, prayerData.isFriday)
            views.setTextViewText(R.id.widget_header_title, headerTitle)

            val remainingMinutes = prayerData.millisUntilNextPrayer / (1000 * 60)
            val remainingSecs = (prayerData.millisUntilNextPrayer / 1000) % 60
            val countdownStr = String.format(Locale.ENGLISH, "+ %02d:%02d", remainingMinutes, remainingSecs)
            views.setTextViewText(R.id.widget_header_countdown, countdownStr)

            // Helper to format prayer time matching screenshot: e.g. "12:53 م"
            fun formatPrayerTimeWithPeriod(prayer: Prayer): String {
                val time = prayerData.getEntry(prayer)?.time ?: return "--:--"
                val hour = if (time.hour % 12 == 0) 12 else time.hour % 12
                val minute = time.minute
                val period = if (settings.isArabic) {
                    if (time.hour >= 12) "م" else "ص"
                } else {
                    if (time.hour >= 12) "PM" else "AM"
                }
                return String.format(Locale.ENGLISH, "%02d:%02d %s", hour, minute, period)
            }

            // 3. Set texts for the 5 prayers: Fajr, Dhuhr, Asr, Maghrib, Isha
            views.setTextViewText(R.id.widget_name_fajr, Prayer.FAJR.getDisplayName(settings.isArabic, prayerData.isFriday))
            views.setTextViewText(R.id.widget_time_fajr, formatPrayerTimeWithPeriod(Prayer.FAJR))

            views.setTextViewText(R.id.widget_name_dhuhr, Prayer.DHUHR.getDisplayName(settings.isArabic, prayerData.isFriday))
            views.setTextViewText(R.id.widget_time_dhuhr, formatPrayerTimeWithPeriod(Prayer.DHUHR))

            views.setTextViewText(R.id.widget_name_asr, Prayer.ASR.getDisplayName(settings.isArabic, prayerData.isFriday))
            views.setTextViewText(R.id.widget_time_asr, formatPrayerTimeWithPeriod(Prayer.ASR))

            views.setTextViewText(R.id.widget_name_maghrib, Prayer.MAGHRIB.getDisplayName(settings.isArabic, prayerData.isFriday))
            views.setTextViewText(R.id.widget_time_maghrib, formatPrayerTimeWithPeriod(Prayer.MAGHRIB))

            views.setTextViewText(R.id.widget_name_isha, Prayer.ISHA.getDisplayName(settings.isArabic, prayerData.isFriday))
            views.setTextViewText(R.id.widget_time_isha, formatPrayerTimeWithPeriod(Prayer.ISHA))

            // 4. Style each column: Active ("الان" in blue), Next ("لاحقا" in blue pill), Normal
            data class WidgetPrayerBinding(
                val prayer: Prayer,
                val colId: Int,
                val tagId: Int,
                val nameId: Int,
                val timeId: Int
            )

            val prayers = listOf(
                WidgetPrayerBinding(Prayer.FAJR, R.id.widget_col_fajr, R.id.widget_tag_fajr, R.id.widget_name_fajr, R.id.widget_time_fajr),
                WidgetPrayerBinding(Prayer.DHUHR, R.id.widget_col_dhuhr, R.id.widget_tag_dhuhr, R.id.widget_name_dhuhr, R.id.widget_time_dhuhr),
                WidgetPrayerBinding(Prayer.ASR, R.id.widget_col_asr, R.id.widget_tag_asr, R.id.widget_name_asr, R.id.widget_time_asr),
                WidgetPrayerBinding(Prayer.MAGHRIB, R.id.widget_col_maghrib, R.id.widget_tag_maghrib, R.id.widget_name_maghrib, R.id.widget_time_maghrib),
                WidgetPrayerBinding(Prayer.ISHA, R.id.widget_col_isha, R.id.widget_tag_isha, R.id.widget_name_isha, R.id.widget_time_isha)
            )

            val tagNow = if (settings.isArabic) "الان" else "NOW"
            val tagNext = if (settings.isArabic) "لاحقا" else "NEXT"

            val colorWhite = android.graphics.Color.WHITE
            val colorDarkSlate = android.graphics.Color.parseColor("#334155")

            prayers.forEach { (prayer, colId, tagId, nameId, timeId) ->
                when {
                    prayer == prayerData.currentPrayer -> {
                        views.setInt(colId, "setBackgroundResource", R.drawable.bg_widget_prayer_active)
                        views.setInt(tagId, "setBackgroundResource", 0)
                        views.setTextViewText(tagId, tagNow)
                        views.setTextColor(tagId, colorWhite)
                        views.setTextColor(nameId, colorWhite)
                        views.setTextColor(timeId, colorWhite)
                    }
                    prayer == prayerData.nextPrayer -> {
                        views.setInt(colId, "setBackgroundResource", R.drawable.bg_widget_prayer_normal)
                        views.setInt(tagId, "setBackgroundResource", R.drawable.bg_widget_tag_next)
                        views.setTextViewText(tagId, tagNext)
                        views.setTextColor(tagId, colorWhite)
                        views.setTextColor(nameId, colorDarkSlate)
                        views.setTextColor(timeId, colorDarkSlate)
                    }
                    else -> {
                        views.setInt(colId, "setBackgroundResource", R.drawable.bg_widget_prayer_normal)
                        views.setInt(tagId, "setBackgroundResource", 0)
                        views.setTextViewText(tagId, "")
                        views.setTextColor(nameId, colorDarkSlate)
                        views.setTextColor(timeId, colorDarkSlate)
                    }
                }
            }

            // 5. Bottom Bar: Hijri date with day name & Gregorian date (e.g. 6-9-2026)
            val hijriInfo = com.example.calculator.HijriCalendarHelper.getHijriDate(prayerData.date)
            val hijriDateStr = if (settings.isArabic) {
                "${hijriInfo.dayNameAr} ${hijriInfo.day} ${hijriInfo.monthNameAr} ${hijriInfo.year}"
            } else {
                "${hijriInfo.dayNameEn} ${hijriInfo.day} ${hijriInfo.monthNameEn} ${hijriInfo.year}"
            }
            val gregorianDateStr = "${prayerData.date.dayOfMonth}-${prayerData.date.monthValue}-${prayerData.date.year}"

            views.setTextViewText(R.id.widget_hijri_date, hijriDateStr)
            views.setTextViewText(R.id.widget_gregorian_date, gregorianDateStr)

            // Click opens app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
