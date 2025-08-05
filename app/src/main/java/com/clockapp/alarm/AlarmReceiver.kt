package com.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.clockapp.ui.alarm.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_RINGTONE_URI = "alarm_ringtone_uri"
        const val EXTRA_ALARM_VIBRATE = "alarm_vibrate"
        const val EXTRA_ALARM_VOLUME = "alarm_volume"
        const val EXTRA_IS_SNOOZE = "is_snooze"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return
        
        val alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
        val ringtoneUri = intent.getStringExtra(EXTRA_ALARM_RINGTONE_URI)
        val vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
        val volume = intent.getIntExtra(EXTRA_ALARM_VOLUME, 50)
        val isSnooze = intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)
        
        // 唤醒屏幕
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "ClockApp:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10分钟超时
        
        // 启动闹钟响铃界面
        val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(EXTRA_ALARM_RINGTONE_URI, ringtoneUri)
            putExtra(EXTRA_ALARM_VIBRATE, vibrate)
            putExtra(EXTRA_ALARM_VOLUME, volume)
            putExtra(EXTRA_IS_SNOOZE, isSnooze)
        }
        
        context.startActivity(alarmIntent)
        
        // 启动闹钟服务
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(EXTRA_ALARM_RINGTONE_URI, ringtoneUri)
            putExtra(EXTRA_ALARM_VIBRATE, vibrate)
            putExtra(EXTRA_ALARM_VOLUME, volume)
        }
        
        context.startForegroundService(serviceIntent)
        
        // 如果不是延迟闹钟，处理重复闹钟的重新调度
        if (!isSnooze) {
            CoroutineScope(Dispatchers.IO).launch {
                // 这里需要依赖注入来获取 repository 和 alarmManager
                // 在实际应用中，应该使用Dagger Hilt或其他DI框架
                // rescheduleRepeatingAlarmIfNeeded(context, alarmId)
            }
        }
        
        wakeLock.release()
    }
}