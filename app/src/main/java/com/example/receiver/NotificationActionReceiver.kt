package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Vibrator

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.example.ACTION_DISMISS_NOTIFICATION"
        const val ACTION_STOP_SOUND = "com.example.ACTION_STOP_SOUND"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_SOUND -> {
                // Cancel vibrations or media
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.cancel()
            }
        }
    }
}
