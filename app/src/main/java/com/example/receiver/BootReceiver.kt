package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.repository.SettingsRepository
import com.example.service.PrayerAlarmScheduler
import com.example.service.PrayerForegroundService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            val settings = SettingsRepository(context).loadSettings()
            PrayerAlarmScheduler.scheduleAllAlarms(context, settings)

            if (settings.persistentNotificationEnabled) {
                val serviceIntent = Intent(context, PrayerForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
