package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.calculator.PrayerTimeCalculator
import com.example.model.Prayer
import com.example.model.PrayerTimesData
import com.example.repository.SettingsRepository
import com.example.widget.PrayerAppWidgetProvider
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PrayerForegroundService : Service() {

    private lateinit var settingsRepo: SettingsRepository
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                updateNotificationAndWidget()
                // Update countdown every 1 second for precise countdown
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            val initialNotification = buildNotification()
            startForeground(NOTIFICATION_ID, initialNotification)
            handler.post(updateRunnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "إشعار مواقيت الصلاة الدائم",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "عرض العد التنازلي ومواقيت الصلاة والتاريخ الهجري"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val settings = settingsRepo.loadSettings()
        val prayerData = PrayerTimeCalculator.calculate(
            date = LocalDate.now(),
            latitude = settings.customLatitude,
            longitude = settings.customLongitude,
            cityNameAr = settings.customCityNameAr,
            cityNameEn = settings.customCityNameEn,
            method = settings.calculationMethod,
            juristicMethod = settings.juristicMethod,
            zoneId = ZoneId.systemDefault()
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification_prayer_persistent)

        val nextPrayerName = prayerData.nextPrayer.getDisplayName(settings.isArabic, prayerData.isFriday)
        val nextPrayerTimeFormatted = prayerData.entries.find { it.prayer == prayerData.nextPrayer }?.timeFormatted ?: ""

        // Set Next Prayer Name (Directly matching screenshot: "العصر")
        remoteViews.setTextViewText(R.id.tv_next_prayer_name, nextPrayerName)

        // Set Countdown Label & Value (Matching screenshot: "الوقت المتبقي" & "m57h 02")
        val countdownLabel = if (settings.isArabic) "الوقت المتبقي" else "Remaining"
        remoteViews.setTextViewText(R.id.tv_countdown_label, countdownLabel)

        val remainingMinutes = (prayerData.millisUntilNextPrayer / (1000 * 60)) % 60
        val remainingHours = prayerData.millisUntilNextPrayer / (1000 * 60 * 60)
        val countdownFormatted = String.format(java.util.Locale.ENGLISH, "m%02dh %02d", remainingMinutes, remainingHours)
        remoteViews.setTextViewText(R.id.tv_countdown_remaining, countdownFormatted)

        // Set Hijri Date (Matching screenshot: "23 ربيع الأول 1448 هـ")
        val hijriText = if (settings.isArabic) prayerData.hijriDateFormattedAr else prayerData.hijriDateFormattedEn
        remoteViews.setTextViewText(R.id.tv_hijri_date, hijriText)

        // PendingIntent to open MainActivity
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque_green_circle)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Persistent ongoing notification
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotificationAndWidget() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        // Update App Widget
        PrayerAppWidgetProvider.updateAllWidgets(this)
    }

    companion object {
        const val CHANNEL_ID = "prayer_persistent_service_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, PrayerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PrayerForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
