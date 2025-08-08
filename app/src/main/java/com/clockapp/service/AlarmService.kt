package com.clockapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.clockapp.MainActivity
import com.clockapp.R
import com.clockapp.ui.alarm.AlarmRingingActivity
import java.io.IOException

class AlarmService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "alarm_notification_channel"
        const val ACTION_START_ALARM = "start_alarm"
        const val ACTION_STOP_ALARM = "stop_alarm"
        const val ACTION_SNOOZE_ALARM = "snooze_alarm"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_RINGTONE_URI = "alarm_ringtone_uri"
        const val EXTRA_ALARM_VIBRATE = "alarm_vibrate"
        const val EXTRA_ALARM_VOLUME = "alarm_volume"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var alarmId: Long = -1
    private var alarmLabel: String = ""
    private var ringtoneUri: String? = null
    private var vibrate: Boolean = true
    private var volume: Int = 50

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START_ALARM -> {
                alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
                alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
                ringtoneUri = intent.getStringExtra(EXTRA_ALARM_RINGTONE_URI)
                vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
                volume = intent.getIntExtra(EXTRA_ALARM_VOLUME, 50)

                startAlarm()
                showAlarmNotification()
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
                stopSelf()
            }
            ACTION_SNOOZE_ALARM -> {
                stopAlarm()
                // 实现贪睡功能
                snoozeAlarm()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startAlarm() {
        // 播放铃声
        playRingtone()

        // 震动
        if (vibrate) {
            startVibration()
        }

        // 启动闹钟响铃Activity
        val alarmIntent = Intent(this, AlarmRingingActivity::class.java)
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        alarmIntent.putExtra(EXTRA_ALARM_ID, alarmId)
        alarmIntent.putExtra(EXTRA_ALARM_LABEL, alarmLabel)
        startActivity(alarmIntent)
    }

    private fun playRingtone() {
        try {
            mediaPlayer = MediaPlayer()

            // 设置铃声来源
            val uri: Uri? = if (ringtoneUri != null) {
                Uri.parse(ringtoneUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            if (uri != null) {
                mediaPlayer?.setDataSource(this, uri)

                // 设置音频属性
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                mediaPlayer?.setAudioAttributes(audioAttributes)

                // 设置音量
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val adjustedVolume = (maxVolume * (volume / 100f)).toInt()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    adjustedVolume,
                    0
                )

                mediaPlayer?.prepare()
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationPattern = longArrayOf(0, 1000, 1000)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(vibrationPattern, 0),
                null
            )
        } else {
            // 旧版本API
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
    }

    private fun snoozeAlarm() {
        // 这里实现贪睡逻辑，例如10分钟后再次响铃
        // 可以使用AlarmManager设置一个新的闹钟
    }

    private fun showAlarmNotification() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_ringing)
            .setContentTitle("闹钟响铃")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_alarm_off, "关闭", stopPendingIntent)
            .addAction(R.drawable.ic_snooze, "贪睡", snoozePendingIntent)
            .setAutoCancel(false)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "闹钟通知"
            val descriptionText = "用于闹钟响铃的通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }
            // 注册渠道
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}