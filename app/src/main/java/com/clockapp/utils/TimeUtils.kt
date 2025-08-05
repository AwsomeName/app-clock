package com.clockapp.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    /**
     * 将小时和分钟转换为字符串格式 HH:mm
     */
    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    /**
     * 获取当前日期的星期几（1-7，其中1代表周一，7代表周日）
     */
    fun getDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        var day = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 将周日=1转换为周日=0
        if (day == 0) day = 7 // 将周日=0转换为周日=7
        return day
    }
    
    /**
     * 根据星期几的数字获取对应的名称（周一至周日）
     */
    fun getDayName(day: Int): String {
        return when (day) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> ""
        }
    }

    /**
     * 获取给定时间戳的日期和时间格式化字符串
     */
    fun formatDateTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }

    /**
     * 获取当前日期的格式化字符串（yyyy-MM-dd）
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * 获取当前时间的格式化字符串（HH:mm:ss）
     */
    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * 获取当前完整的日期和时间（yyyy-MM-dd HH:mm:ss）
     */
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * 计算下一个闹钟时间
     * 
     * @param hour 小时
     * @param minute 分钟
     * @param repeatDays 重复天数集合，如果为空则表示一次性闹钟
     * @return 下一次闹钟的时间戳，单位为毫秒
     */
    fun getNextAlarmTime(hour: Int, minute: Int, repeatDays: Set<Int>?): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val currentTimeMillis = System.currentTimeMillis()
        
        // 如果是非重复闹钟
        if (repeatDays == null || repeatDays.isEmpty()) {
            // 如果设置的时间已经过去，设置为明天的这个时间
            if (calendar.timeInMillis <= currentTimeMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }
        
        // 如果是重复闹钟
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var dayOfWeekInt = dayOfWeek - 1 // 将周日=1转换为周日=0
        if (dayOfWeekInt == 0) dayOfWeekInt = 7 // 将周日=0转换为周日=7
        
        // 如果当前时间已过或者今天不在重复天数内，需要寻找下一个重复的日子
        if (calendar.timeInMillis <= currentTimeMillis || !repeatDays.contains(dayOfWeekInt)) {
            var daysToAdd = 1
            var nextDay = dayOfWeekInt
            
            // 最多循环7天，找到下一个重复的日子
            for (i in 1..7) {
                nextDay = if (nextDay == 7) 1 else nextDay + 1
                if (repeatDays.contains(nextDay)) {
                    break
                }
                daysToAdd++
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        
        return calendar.timeInMillis
    }
}