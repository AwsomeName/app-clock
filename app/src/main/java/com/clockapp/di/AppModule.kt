package com.clockapp.di

import android.content.Context
import androidx.room.Room
import com.clockapp.alarm.ClockAlarmManager
import com.clockapp.data.dao.AlarmDao
import com.clockapp.data.database.AlarmDatabase
import com.clockapp.data.repository.AlarmRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(@ApplicationContext context: Context): AlarmDatabase {
        return Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            "alarm_database"
        ).build()
    }

    @Provides
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(alarmDao: AlarmDao): AlarmRepository {
        return AlarmRepository(alarmDao)
    }

    @Provides
    @Singleton
    fun provideClockAlarmManager(@ApplicationContext context: Context): ClockAlarmManager {
        return ClockAlarmManager(context)
    }
}