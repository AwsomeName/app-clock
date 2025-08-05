package com.clockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val repeatDays: Set<Int> = emptySet(), // 0=周日, 1=周一, ..., 6=周六
    val ringtoneUri: String? = null,
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 10, // 延迟分钟数
    val volume: Int = 50, // 音量 0-100
    val createdAt: Long = System.currentTimeMillis(),
    val isOneTime: Boolean = false, // 是否为一次性闹钟
    val specificDate: Long? = null // 特定日期时间戳（用于生日、节日等）
) {
    fun getNextAlarmTime(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // 如果是一次性闹钟且有特定日期
        if (isOneTime && specificDate != null) {
            calendar.timeInMillis = specificDate
            return calendar
        }
        
        // 如果是重复闹钟
        if (repeatDays.isNotEmpty()) {
            val now = Calendar.getInstance()
            var found = false
            
            // 检查今天是否在重复日期中
            val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1
            if (repeatDays.contains(todayDayOfWeek) && calendar.after(now)) {
                return calendar
            }
            
            // 寻找下一个重复日期
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (repeatDays.contains(dayOfWeek)) {
                    found = true
                    break
                }
            }
            
            if (!found) {
                // 理论上不应该到这里，但为了安全起见设置为明天
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        } else {
            // 一次性闹钟，如果时间已过则设置为明天
            val now = Calendar.getInstance()
            if (calendar.before(now) || calendar.equals(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar
    }
    
    fun getRepeatDaysString(): String {
        if (repeatDays.isEmpty()) return "一次"
        if (repeatDays.size == 7) return "每天"
        
        val dayNames = arrayOf("日", "一", "二", "三", "四", "五", "六")
        val workdays = setOf(1, 2, 3, 4, 5)
        val weekends = setOf(0, 6)
        
        return when {
            repeatDays == workdays -> "工作日"
            repeatDays == weekends -> "周末"
            else -> repeatDays.sorted().joinToString(", ") { "周${dayNames[it]}" }
        }
    }
    
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}