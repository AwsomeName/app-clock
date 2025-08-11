package com.clockapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.clockapp.utils.AlarmScheduler
import com.clockapp.data.model.Alarm
import com.clockapp.service.AlarmService
import kotlinx.serialization.json.Json

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "闹钟广播接收器收到意图: ${intent.action}")

        when (intent.action) {
            // 闹钟触发
            ACTION_TRIGGER_ALARM -> {
                val alarmJson = intent.getStringExtra(EXTRA_ALARM)
                if (alarmJson != null) {
                    try {
                        val alarm = Json.decodeFromString<Alarm>(alarmJson)
                        triggerAlarm(context, alarm)

                        // 如果是重复闹钟，重新安排下次响铃
                        if (alarm.repeatDays.isNotEmpty()) {
                            AlarmScheduler.scheduleAlarm(context, alarm)
                        } else {
                            // 如果是一次性闹钟，设置为禁用状态
                            // 这里不直接更新数据库，而是通过广播通知UI更新
                            val updateIntent = Intent(ACTION_UPDATE_ALARM_STATE)
                            updateIntent.putExtra(EXTRA_ALARM_ID, alarm.id)
                            updateIntent.putExtra(EXTRA_ALARM_ENABLED, false)
                            context.sendBroadcast(updateIntent)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "解析闹钟数据失败: ${e.message}")
                    }
                }
            }

            // 设备启动完成
            Intent.ACTION_BOOT_COMPLETED -> {
                // 重启所有已启用的闹钟
                AlarmScheduler.rescheduleAllAlarms(context)
            }

            // 时区变化
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // 重新安排所有已启用的闹钟
                AlarmScheduler.rescheduleAllAlarms(context)
            }

            // 时间设置变化
            Intent.ACTION_TIME_CHANGED -> {
                // 重新安排所有已启用的闹钟
                AlarmScheduler.rescheduleAllAlarms(context)
            }
        }
    }

    private fun triggerAlarm(context: Context, alarm: Alarm) {
        // 启动闹钟服务
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmService.EXTRA_ALARM_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(AlarmService.EXTRA_ALARM_VIBRATE, alarm.vibrate)
            putExtra(AlarmService.EXTRA_ALARM_VOLUME, alarm.volume)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"

        // 操作常量
        const val ACTION_TRIGGER_ALARM = "com.clockapp.ACTION_TRIGGER_ALARM"
        const val ACTION_UPDATE_ALARM_STATE = "com.clockapp.ACTION_UPDATE_ALARM_STATE"

        // 额外数据常量
        const val EXTRA_ALARM = "extra_alarm"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_ENABLED = "extra_alarm_enabled"
    }
}