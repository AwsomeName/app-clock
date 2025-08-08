package com.clockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clockapp.alarm.ClockAlarmManager
import com.clockapp.data.AlarmDatabase
import com.clockapp.data.model.Alarm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AlarmWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val alarmDatabase: AlarmDatabase,
    private val clockAlarmManager: ClockAlarmManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val alarmId = inputData.getLong(EXTRA_ALARM_ID, -1L)
                if (alarmId == -1L) {
                    return@withContext Result.failure()
                }

                val alarmDao = alarmDatabase.alarmDao()
                val alarm = alarmDao.getAlarmById(alarmId)

                alarm?.let {
                    // 检查闹钟是否启用
                    if (it.isEnabled) {
                        // 设置下一次闹钟
                        clockAlarmManager.setAlarm(it)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}