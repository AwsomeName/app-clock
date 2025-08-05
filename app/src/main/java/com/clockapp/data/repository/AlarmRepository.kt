package com.clockapp.data.repository

import androidx.lifecycle.LiveData
import com.clockapp.data.dao.AlarmDao
import com.clockapp.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    
    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)
    
    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)
    
    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    
    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)
    
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)
    
    suspend fun updateAlarmEnabled(id: Long, enabled: Boolean) {
        alarmDao.updateAlarmEnabled(id, enabled)
    }
    
    suspend fun getRepeatingAlarms(): List<Alarm> = alarmDao.getRepeatingAlarms()
    
    suspend fun getOneTimeAlarms(): List<Alarm> = alarmDao.getOneTimeAlarms()
    
    suspend fun deleteExpiredOneTimeAlarms() {
        val currentTime = System.currentTimeMillis()
        alarmDao.deleteExpiredOneTimeAlarms(currentTime)
    }
}