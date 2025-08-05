package com.clockapp.utils

import com.clockapp.data.model.Alarm
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 用于开发和测试的示例闹钟数据
 */
object SampleData {
    
    /**
     * 生成一系列示例闹钟数据
     */
    fun generateSampleAlarms(): List<Alarm> {
        return listOf(
            Alarm(
                id = 1,
                hour = 7,
                minute = 30,
                label = "起床闹钟",
                enabled = true,
                repeatDays = listOf(1, 2, 3, 4, 5), // 工作日
                vibrate = true,
                ringtoneName = "默认铃声",
                ringtoneUri = "android.resource://com.clockapp/raw/default_alarm",
                snoozeEnabled = true,
                snoozeDuration = 5,
                nextAlarmTime = LocalDateTime.now().plusDays(1)
                    .withHour(7).withMinute(30).withSecond(0)
            ),
            Alarm(
                id = 2,
                hour = 9,
                minute = 0,
                label = "晨会提醒",
                enabled = true,
                repeatDays = listOf(1, 3, 5), // 周一、周三、周五
                vibrate = false,
                ringtoneName = "轻松铃声",
                ringtoneUri = "android.resource://com.clockapp/raw/gentle_alarm",
                snoozeEnabled = false,
                snoozeDuration = 0,
                nextAlarmTime = LocalDateTime.now().plusDays(2)
                    .withHour(9).withMinute(0).withSecond(0)
            ),
            Alarm(
                id = 3,
                hour = 22,
                minute = 0,
                label = "睡前提醒",
                enabled = true,
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6), // 每天
                vibrate = true,
                ringtoneName = "活力铃声",
                ringtoneUri = "android.resource://com.clockapp/raw/energetic_alarm",
                snoozeEnabled = true,
                snoozeDuration = 10,
                nextAlarmTime = LocalDateTime.now()
                    .withHour(22).withMinute(0).withSecond(0)
            )
        )
    }
    
    /**
     * 创建新的空闹钟对象，使用当前时间作为初始值
     */
    fun createEmptyAlarm(): Alarm {
        val now = LocalTime.now()
        return Alarm(
            id = 0, // 0表示新闹钟，将由Room自动分配ID
            hour = now.hour,
            minute = now.minute,
            label = "",
            enabled = true,
            repeatDays = emptyList(),
            vibrate = true,
            ringtoneName = "默认铃声",
            ringtoneUri = "android.resource://com.clockapp/raw/default_alarm",
            snoozeEnabled = true,
            snoozeDuration = 5,
            nextAlarmTime = null
        )
    }
}