package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.repository.SettingsRepository
import com.example.service.PrayerAlarmScheduler
import com.example.ui.FullscreenAdhanActivity
import com.example.ui.PreAlertActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val remainingMinutes = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_REMAINING_MINUTES, 15)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "PrayerTimes:AlarmWakeLock"
        )
        wakeLock?.acquire(15000L) // 15 seconds

        when (action) {
            PrayerAlarmScheduler.ACTION_PRAYER_ADHAN -> {
                handleAdhan(context, prayerName)
            }
            PrayerAlarmScheduler.ACTION_PRE_ALERT -> {
                handlePreAlert(context, prayerName, remainingMinutes)
            }
            PrayerAlarmScheduler.ACTION_SUHOOR_ALERT -> {
                handleSuhoorAlert(context)
            }
        }

        // Re-schedule alarms to ensure continuity
        val repo = SettingsRepository(context)
        PrayerAlarmScheduler.scheduleAllAlarms(context, repo.loadSettings())
    }

    private fun handleAdhan(context: Context, prayerName: String) {
        // 1. Launch Fullscreen Adhan Activity (Full screen video, no text, double tap/volume keys to dismiss)
        val adhanIntent = Intent(context, FullscreenAdhanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(FullscreenAdhanActivity.EXTRA_PRAYER_NAME, prayerName)
        }

        try {
            context.startActivity(adhanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Also present a high priority heads-up notification with full screen intent
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "adhan_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات الأذان (Adhan)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات دخول وقت الأذان"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            101,
            adhanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mosque_green_circle)
            .setContentTitle("حان الآن وقت $prayerName")
            .setContentText("حي على الصلاة، حي على الفلاح")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2001, notification)
    }

    private fun handlePreAlert(context: Context, prayerName: String, remainingMinutes: Int) {
        val preAlertIntent = Intent(context, PreAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PreAlertActivity.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PreAlertActivity.EXTRA_REMAINING_MINUTES, remainingMinutes)
        }

        try {
            context.startActivity(preAlertIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pre_alert_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "التنبيه المسبق للصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة التنبيه المسبق قبل الأذان"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            preAlertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = "يتبقى على صلاة $prayerName $remainingMinutes دقيقة"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mosque_green_circle)
            .setContentTitle("اقترب وقت الصلاة")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }

    private fun handleSuhoorAlert(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "suhoor_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيه المسحراتي (السحور)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات وقت السحور والمسحراتي"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mosque_green_circle)
            .setContentTitle("تنبيه المسحراتي - وقت السحور")
            .setContentText("اصحى يا نايم وحّد الدايم.. السحور بركة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2003, notification)
    }
}
