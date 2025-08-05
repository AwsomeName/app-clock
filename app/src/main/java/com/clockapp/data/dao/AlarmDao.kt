package com.clockapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clockapp.data.model.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): Flow<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): Alarm?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)
    
    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateAlarmEnabled(id: Long, enabled: Boolean)
    
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 AND repeatDays != '' ORDER BY hour, minute")
    suspend fun getRepeatingAlarms(): List<Alarm>
    
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 AND isOneTime = 1 ORDER BY specificDate")
    suspend fun getOneTimeAlarms(): List<Alarm>
    
    @Query("DELETE FROM alarms WHERE isOneTime = 1 AND specificDate < :currentTime")
    suspend fun deleteExpiredOneTimeAlarms(currentTime: Long)
}