package com.clockapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.clockapp.R
import com.clockapp.data.model.Alarm
import com.clockapp.ui.alarm.AlarmRingingActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alarm: Alarm? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化震动器
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        // 获取唤醒锁，确保CPU在屏幕关闭时继续运行
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ClockApp:AlarmServiceWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10分钟后自动释放
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmJson = intent.getStringExtra(EXTRA_ALARM)
                alarmJson?.let {
                    alarm = Json.decodeFromString<Alarm>(it)
                    startForeground(NOTIFICATION_ID, createNotification())
                    startAlarm()
                    showAlarmActivity()
                }
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
                stopForeground(true)
                stopSelf()
            }
            ACTION_SNOOZE_ALARM -> {
                stopAlarm()
                // 处理推迟逻辑在AlarmRingingActivity中实现
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun createNotification(): Notification {
        val channelId = createNotificationChannel()
        
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // 传递闹钟数据
            alarm?.let {
                putExtra(AlarmRingingActivity.EXTRA_ALARM, Json.encodeToString(it))
            }
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm_ringing)
            .setContentTitle(getString(R.string.alarm_ringing))
            .setContentText(alarm?.label ?: getString(R.string.alarm))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_ID
            val channelName = getString(R.string.alarm_notification_channel)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration = true
                enableLights(true)
                setBypassDnd(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            return channelId
        }
        return ""
    }
    
    private fun startAlarm() {
        // 播放铃声
        startRingtone()
        
        // 开始震动
        alarm?.let {
            if (it.vibrate) {
                startVibration()
            }
        }
    }
    
    private fun startRingtone() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        
        try {
            // 获取铃声URI
            val ringtoneUri = if (alarm?.ringtoneUri.isNullOrEmpty()) {
                // 如果未设置铃声，使用默认铃声
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } else {
                Uri.parse(alarm?.ringtoneUri)
            }
            
            // 特殊处理：静音选项
            if (ringtoneUri.toString().endsWith("silent")) {
                // 不播放声音
                return
            }
            
            mediaPlayer?.apply {
                setDataSource(applicationContext, ringtoneUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 如果指定的铃声无法播放，使用默认铃声
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer?.apply {
                    reset()
                    setDataSource(applicationContext, defaultUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }
    
    private fun startVibration() {
        val pattern = longArrayOf(0, 800, 800, 800) // 震动模式：800ms震动，800ms停止
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0) // 重复从索引0开始
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0) // 重复从索引0开始
        }
    }
    
    private fun stopAlarm() {
        // 停止媒体播放
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        // 停止振动
        vibrator?.cancel()
    }
    
    private fun showAlarmActivity() {
        // 启动全屏活动显示闹钟响铃界面
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // 传递闹钟数据
            alarm?.let {
                putExtra(AlarmRingingActivity.EXTRA_ALARM, Json.encodeToString(it))
            }
        }
        startActivity(fullScreenIntent)
    }
    
    override fun onDestroy() {
        stopAlarm()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "alarm_service_channel"
        
        const val EXTRA_ALARM = "extra_alarm"
        
        const val ACTION_START_ALARM = "com.clockapp.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.clockapp.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.clockapp.ACTION_SNOOZE_ALARM"
    }
}