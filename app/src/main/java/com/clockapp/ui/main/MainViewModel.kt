package com.clockapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clockapp.alarm.ClockAlarmManager
import com.clockapp.data.model.Alarm
import com.clockapp.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmManager: ClockAlarmManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    val alarms = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // 清理过期的一次性闹钟
        viewModelScope.launch {
            repository.deleteExpiredOneTimeAlarms()
        }
    }
    
    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                val id = repository.insertAlarm(alarm)
                val newAlarm = alarm.copy(id = id)
                if (newAlarm.isEnabled) {
                    alarmManager.setAlarm(newAlarm)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "添加闹钟失败: ${e.message}"
                )
            }
        }
    }
    
    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                repository.updateAlarm(alarm)
                
                // 取消旧的闹钟
                alarmManager.cancelAlarm(alarm.id)
                
                // 如果启用，重新调度
                if (alarm.isEnabled) {
                    alarmManager.setAlarm(alarm)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新闹钟失败: ${e.message}"
                )
            }
        }
    }
    
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(alarm)
                alarmManager.cancelAlarm(alarm.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除闹钟失败: ${e.message}"
                )
            }
        }
    }
    
    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
                repository.updateAlarm(updatedAlarm)
                
                if (updatedAlarm.isEnabled) {
                    alarmManager.setAlarm(updatedAlarm)
                } else {
                    alarmManager.cancelAlarm(alarm.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "切换闹钟状态失败: ${e.message}"
                )
            }
        }
    }
    
    fun skipNextAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                // 如果是重复闹钟，临时禁用一次
                if (alarm.repeatDays.isNotEmpty()) {
                    // 计算下次响铃时间，跳过最近一次
                    alarmManager.cancelAlarm(alarm.id)
                    
                    // 重新计算下次闹钟时间（跳过今天/明天的一次）
                    val nextTime = alarm.getNextAlarmTime()
                    nextTime.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    
                    // 创建新的闹钟时间并重新调度
                    alarmManager.setAlarm(alarm)
                } else {
                    // 一次性闹钟直接禁用
                    toggleAlarm(alarm)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "跳过闹钟失败: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun showAddAlarmDialog() {
        _uiState.value = _uiState.value.copy(showAddAlarmDialog = true)
    }
    
    fun hideAddAlarmDialog() {
        _uiState.value = _uiState.value.copy(showAddAlarmDialog = false)
    }
    
    fun showEditAlarmDialog(alarm: Alarm) {
        _uiState.value = _uiState.value.copy(
            showEditAlarmDialog = true,
            editingAlarm = alarm
        )
    }
    
    fun hideEditAlarmDialog() {
        _uiState.value = _uiState.value.copy(
            showEditAlarmDialog = false,
            editingAlarm = null
        )
    }
}

data class MainUiState(
    val showAddAlarmDialog: Boolean = false,
    val showEditAlarmDialog: Boolean = false,
    val editingAlarm: Alarm? = null,
    val errorMessage: String? = null
)