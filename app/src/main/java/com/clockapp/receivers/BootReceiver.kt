package com.clockapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.clockapp.utils.AlarmScheduler

/**
 * 启动完成广播接收器
 * 用于在设备重启后重新安排所有闹钟
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "设备启动完成，重新安排闹钟")
            
            // 重新安排所有闹钟
            AlarmScheduler.rescheduleAlarmsAfterReboot(context)
        }
    }
    
    companion object {
        private const val TAG = "BootReceiver"
    }
}