package com.clockapp.alarm

import android.content.Context
import androidx.work.*
import com.clockapp.data.database.AlarmDatabase
import com.clockapp.data.model.Alarm
import com.clockapp.data.repository.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val alarmDao = AlarmDatabase.getInstance(context).alarmDao()
    private val alarmRepository = AlarmRepository(alarmDao)
    private val clockAlarmManager = ClockAlarmManager(context)

    override suspend fun doWork(): Result {
        // 获取所有启用的闹钟
        val enabledAlarms = alarmRepository.getEnabledAlarms().first()

        // 删除过期的一次性闹钟
        alarmRepository.deleteExpiredOneTimeAlarms()

        // 为每一个启用的闹钟设置闹钟
        for (alarm in enabledAlarms) {
            scheduleAlarm(alarm)
        }

        // 设置下一次工作器运行时间为明天
        scheduleNextWork()
        
        return Result.success()
    }

    private suspend fun scheduleAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            // 获取下一个闹钟时间
            val nextAlarmTime = alarm.getNextAlarmTime()
            if (nextAlarmTime != null) {
                // 调度闹钟
                clockAlarmManager.scheduleAlarm(
                    alarm.id,
                    alarm.hour,
                    alarm.minute,
                    nextAlarmTime,
                    alarm.label,
                    alarm.ringtoneUri,
                    alarm.vibrate,
                    alarm.snoozeEnabled,
                    alarm.snoozeInterval,
                    alarm.snoozeTimes
                )
            }
        }
    }

    private fun scheduleNextWork() {
        val calendar = Calendar.getInstance().apply {
            // 设置为明天的午夜
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()
        val delayMillis = calendar.timeInMillis - now.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(ALARM_WORKER_TAG)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                ALARM_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    companion object {
        private const val ALARM_WORKER_TAG = "alarm_worker_tag"
        private const val ALARM_WORKER_NAME = "alarm_worker_name"

        fun scheduleDaily(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
                .addTag(ALARM_WORKER_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ALARM_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}