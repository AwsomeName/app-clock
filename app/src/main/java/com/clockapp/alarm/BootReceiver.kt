package com.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * 接收系统启动广播，当设备重启后重新调度所有闹钟
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "收到启动广播: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            // 在Android 10及以上版本，系统启动完成后可能需要等待几秒钟
            // 这样系统服务才能完全初始化
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Thread.sleep(2000)
            }
            
            // 启动工作器重新调度所有闹钟
            AlarmWorker.scheduleDaily(context)
            
            Log.d(TAG, "已启动闹钟工作器重新调度闹钟")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}