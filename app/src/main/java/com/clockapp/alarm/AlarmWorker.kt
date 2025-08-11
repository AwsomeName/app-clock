package com.clockapp.alarm

import android.content.Context
import androidx.work.*
import com.clockapp.data.AlarmDatabase
import com.clockapp.data.model.Alarm
import com.clockapp.data.repository.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val alarmDao = AlarmDatabase.getDatabase(applicationContext).alarmDao()
    private val alarmRepository = AlarmRepository(alarmDao)
    private val clockAlarmManager = ClockAlarmManager(applicationContext)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 获取所有已启用的闹钟
                val enabledAlarms = alarmRepository.getEnabledAlarms().first()

                // 为每个闹钟安排下一次响铃
                for (alarm in enabledAlarms) {
                    clockAlarmManager.setAlarm(alarm)
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AlarmWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "AlarmRescheduler",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}