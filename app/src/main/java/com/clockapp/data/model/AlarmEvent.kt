package com.clockapp.data.model

/**
 * 闹钟事件处理类
 * 用于封装闹钟操作的事件，在UI和Service之间传递闹钟操作信息
 */
sealed class AlarmEvent {
    /**
     * 创建新闹钟事件
     */
    data class Create(val alarm: Alarm) : AlarmEvent()
    
    /**
     * 更新闹钟事件
     */
    data class Update(val alarm: Alarm) : AlarmEvent()
    
    /**
     * 删除闹钟事件
     */
    data class Delete(val alarm: Alarm) : AlarmEvent()
    
    /**
     * 启用/禁用闹钟事件
     */
    data class Toggle(val alarm: Alarm, val enabled: Boolean) : AlarmEvent()
    
    /**
     * 暂停闹钟事件
     */
    data class Snooze(val alarmId: Long, val duration: Int) : AlarmEvent()
    
    /**
     * 关闭闹钟事件
     */
    data class Dismiss(val alarmId: Long) : AlarmEvent()
    
    /**
     * 测试闹钟事件（预览铃声和震动）
     */
    data class Test(val alarm: Alarm) : AlarmEvent()
}