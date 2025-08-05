package com.clockapp.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.clockapp.utils.AlarmScheduler
import com.clockapp.utils.SampleData

/**
 * 闹钟重新安排工作器
 * 周期性运行，确保闹钟不会因系统原因被取消
 */
class AlarmReschedulerWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        Log.d(TAG, "闹钟重新安排工作开始执行")
        
        try {
            // 获取所有已启用的闹钟并重新安排
            // 这里为了简化示例，使用SampleData获取示例闹钟
            val alarms = SampleData.generateSampleAlarms()
                .filter { it.enabled }
            
            Log.d(TAG, "找到${alarms.size}个已启用的闹钟需要重新安排")
            
            for (alarm in alarms) {
                AlarmScheduler.scheduleAlarm(applicationContext, alarm)
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "重新安排闹钟时发生错误: ${e.message}")
            return Result.failure()
        }
    }
    
    companion object {
        private const val TAG = "AlarmReschedulerWorker"
    }
}