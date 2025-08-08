package com.clockapp.data.database

import android.content.Context
import androidx.room.*
import com.clockapp.data.dao.AlarmDao
import com.clockapp.data.model.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@TypeConverters(Converters::class)
@Database(
    entities = [Alarm::class],
    version = 1,
    exportSchema = false
)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database"
                )
                    .addCallback(AlarmDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AlarmDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: RoomDatabase) {
                super.onCreate(db)
                // 可以在这里添加一些默认数据
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromIntSet(value: Set<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntSet(value: String): Set<Int> {
        return if (value.isEmpty()) {
            emptySet()
        } else {
            value.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }
}