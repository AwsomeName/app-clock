package com.clockapp.alarm

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
        private const val CHANNEL_ID = "alarm_channel"
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
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                stopAlarm()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE_ALARM -> {
                snoozeAlarm()
                return START_NOT_STICKY
            }
            else -> {
                alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
                alarmLabel = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
                val ringtoneUri = intent?.getStringExtra(EXTRA_ALARM_RINGTONE_URI)
                val vibrate = intent?.getBooleanExtra(EXTRA_ALARM_VIBRATE, true) ?: true
                val volume = intent?.getIntExtra(EXTRA_ALARM_VOLUME, 50) ?: 50
                
                startForeground(NOTIFICATION_ID, createNotification())
                startAlarmRinging(ringtoneUri, vibrate, volume)
                
                // 10分钟后自动停止
                handler.postDelayed({
                    stopAlarm()
                }, 10 * 60 * 1000)
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "闹钟通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "闹钟响铃通知"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this, 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("闹钟")
            .setContentText(if (alarmLabel.isNotEmpty()) alarmLabel else "闹钟时间到了")
            .setSmallIcon(R.drawable.ic_alarm)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_alarm_off, "关闭", stopPendingIntent)
            .addAction(R.drawable.ic_snooze, "延迟", snoozePendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    private fun startAlarmRinging(ringtoneUri: String?, vibrate: Boolean, volume: Int) {
        try {
            mediaPlayer = MediaPlayer().apply {
                val uri = if (ringtoneUri != null) {
                    Uri.parse(ringtoneUri)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
                
                setDataSource(this@AlarmService, uri)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                }
                
                isLooping = true
                setVolume(volume / 100f, volume / 100f)
                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        if (vibrate) {
            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }
    
    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        vibrator?.cancel()
        handler.removeCallbacksAndMessages(null)
        
        stopForeground(true)
        stopSelf()
    }
    
    private fun snoozeAlarm() {
        // 这里应该调用 AlarmManager 来设置延迟闹钟
        // 由于需要访问数据库和依赖注入，这里只是停止当前闹钟
        stopAlarm()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}