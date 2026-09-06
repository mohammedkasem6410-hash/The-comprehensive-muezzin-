package com.example.ui

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.R
import com.example.repository.SettingsRepository

class FullscreenAdhanActivity : ComponentActivity() {

    private var videoView: VideoView? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var gestureDetector: GestureDetector

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn screen on and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive Fullscreen - Hides Status Bar & Navigation Bar completely
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Create Container View with NO TEXT WHATSOEVER as requested
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        // VideoView occupying full screen
        videoView = VideoView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootLayout.addView(videoView)
        setContentView(rootLayout)

        // Double tap detection to stop and finish
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                stopAndFinish()
                return true
            }

            override fun onDown(e: MotionEvent): Boolean = true
        })

        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        videoView?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        playAdhanMedia()
    }

    private fun playAdhanMedia() {
        val settings = SettingsRepository(this).loadSettings()
        val customVideoUri = settings.adhanVideoUri

        if (!customVideoUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(customVideoUri)
                videoView?.setVideoURI(uri)
                videoView?.setOnPreparedListener { mp ->
                    mp.isLooping = false
                    mp.start()
                }
                videoView?.setOnCompletionListener {
                    stopAndFinish()
                }
                videoView?.setOnErrorListener { _, _, _ ->
                    fallbackPlayAudio()
                    true
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback or default adhan tone with black / Islamic serene backdrop
        fallbackPlayAudio()
    }

    private fun fallbackPlayAudio() {
        try {
            // Play alarm / notification tone or bundled audio
            val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@FullscreenAdhanActivity, alertUri)
                prepare()
                isLooping = false
                start()
                setOnCompletionListener {
                    stopAndFinish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Dismiss adhan on Volume Up, Volume Down, or Volume Mute
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE -> {
                stopAndFinish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun stopAndFinish() {
        try {
            videoView?.stopPlayback()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finish()
    }

    override fun onDestroy() {
        stopAndFinish()
        super.onDestroy()
    }
}
