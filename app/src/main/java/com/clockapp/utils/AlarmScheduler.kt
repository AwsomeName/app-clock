package com.clockapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.clockapp.data.model.Alarm
import com.clockapp.receivers.AlarmReceiver
import com.clockapp.work.AlarmReschedulerWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * 闹钟调度器
 * 负责闹钟的调度、设置和取消
 */
object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    
    /**
     * 安排闹钟
     */
    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) {
            Log.d(TAG, "闹钟已禁用，不进行调度: ${alarm.id}")
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM, Json.encodeToString(alarm))
        }
        
        // 使用闹钟ID作为请求码，确保每个闹钟有唯一的PendingIntent
        val requestCode = alarm.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 获取下一次闹钟时间
        val nextAlarmTime = alarm.getNextAlarmTime()
        val triggerTimeMillis = nextAlarmTime.timeInMillis
        
        // 设置闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "无法设置精确闹钟，使用非精确闹钟: ${alarm.id}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "使用精确闹钟并允许设备唤醒: ${alarm.id}")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        } else {
            Log.d(TAG, "使用精确闹钟: ${alarm.id}")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
        
        // 记录下一次闹钟时间
        val nextTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", nextAlarmTime.time).toString()
        Log.d(TAG, "已安排闹钟: ${alarm.id}, 下次响铃时间: $nextTime")
        
        // 初始化周期性重新安排任务
        initRescheduleWorker(context)
    }
    
    /**
     * 取消闹钟
     */
    fun cancelAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        
        val requestCode = alarm.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "已取消闹钟: ${alarm.id}")
        } else {
            Log.d(TAG, "未找到要取消的闹钟PendingIntent: ${alarm.id}")
        }
    }
    
    /**
     * 在设备重启后重新安排所有闹钟
     */
    fun rescheduleAlarmsAfterReboot(context: Context) {
        Log.d(TAG, "在设备重启后重新安排所有闹钟")
        
        // 这里需要实现从数据库读取所有闹钟并重新安排
        // 为简化示例，使用SampleData获取示例闹钟
        val alarms = SampleData.generateSampleAlarms()
        
        for (alarm in alarms) {
            if (alarm.isEnabled) {
                scheduleAlarm(context, alarm)
            }
        }
        
        // 初始化周期性重新安排任务
        initRescheduleWorker(context)
    }
    
    /**
     * 重新安排所有闹钟
     * 用于时间或时区变更时
     */
    fun rescheduleAllAlarms(context: Context) {
        Log.d(TAG, "重新安排所有闹钟")
        
        // 这里需要实现从数据库读取所有闹钟并重新安排
        // 为简化示例，使用SampleData获取示例闹钟
        val alarms = SampleData.generateSampleAlarms()
        
        for (alarm in alarms) {
            if (alarm.isEnabled) {
                // 先取消，再重新安排
                cancelAlarm(context, alarm)
                scheduleAlarm(context, alarm)
            }
        }
    }
    
    /**
     * 初始化周期性重新安排任务
     * 防止系统清理闹钟
     */
    private fun initRescheduleWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<AlarmReschedulerWorker>(
            15, TimeUnit.MINUTES
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "alarm_reschedule_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Log.d(TAG, "已启动闹钟周期性重新安排任务")
    }
}