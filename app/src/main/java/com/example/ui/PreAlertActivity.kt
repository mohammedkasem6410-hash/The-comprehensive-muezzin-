package com.example.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.repository.SettingsRepository

class PreAlertActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_REMAINING_MINUTES = "extra_remaining_minutes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val remainingMinutes = intent.getIntExtra(EXTRA_REMAINING_MINUTES, 15)

        playChime()

        val settings = SettingsRepository(this).loadSettings()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF131E18)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A),
                                        Color(0xFF0B0F17)
                                    )
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pre_alert_card"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.96f)),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(Color(0x338B5E3C), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = "Alert",
                                        tint = Color(0xFFFDBA74),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = if (settings.isArabic) "اقترب موعد الأذان" else "Prayer Approaching",
                                    fontSize = 18.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )

                                // Required sentence: "يتبقى على صلاة [اسم الصلاة]"
                                Text(
                                    text = if (settings.isArabic) {
                                        "يتبقى على صلاة $prayerName"
                                    } else {
                                        "Time remaining for $prayerName"
                                    },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDBA74),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = if (settings.isArabic) "$remainingMinutes دقيقة" else "$remainingMinutes minutes",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Text(
                                    text = if (settings.isArabic) {
                                        "تأهب للوضوء واستعد لأداء الصلاة في وقتها"
                                    } else {
                                        "Prepare for wudu and get ready for prayer"
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { stopSound() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("mute_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
                                    ) {
                                        Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (settings.isArabic) "كتم" else "Mute")
                                    }

                                    Button(
                                        onClick = {
                                            stopSound()
                                            finish()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("dismiss_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (settings.isArabic) "حسناً" else "Dismiss")
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        stopSound()
                                        startActivity(Intent(this@PreAlertActivity, MainActivity::class.java))
                                        finish()
                                    },
                                    modifier = Modifier.testTag("open_app_button")
                                ) {
                                    Text(
                                        if (settings.isArabic) "فتح التطبيق" else "Open App",
                                        color = Color(0xFF60A5FA),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun playChime() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@PreAlertActivity, alertUri)
                prepare()
                isLooping = false
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopSound()
        super.onDestroy()
    }
}
