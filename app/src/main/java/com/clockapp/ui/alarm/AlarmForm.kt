package com.clockapp.ui.alarm

import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clockapp.data.model.Alarm
import java.util.*

@Composable
fun AlarmForm(
    alarm: Alarm,
    onAlarmChanged: (Alarm) -> Unit
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
    ) {
        // 时间设置
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { showTimePicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.displayMedium
                )
                
                Text(
                    text = if (alarm.hour < 12) "上午" else "下午",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        if (showTimePicker) {
            TimePickerDialog(
                initialHour = alarm.hour,
                initialMinute = alarm.minute,
                onTimeSelected = { hour, minute ->
                    onAlarmChanged(alarm.copy(hour = hour, minute = minute))
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 闹钟标签
        OutlinedTextField(
            value = alarm.label,
            onValueChange = { onAlarmChanged(alarm.copy(label = it)) },
            label = { Text("闹钟标签") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Label, contentDescription = "标签")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 重复设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "重复",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RepeatDaysSelector(
                    selectedDays = alarm.repeatDays,
                    onDaysSelected = { onAlarmChanged(alarm.copy(repeatDays = it)) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 振动设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "振动"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "振动")
                }
                
                Switch(
                    checked = alarm.vibrate,
                    onCheckedChange = { onAlarmChanged(alarm.copy(vibrate = it)) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 延迟设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "延迟"
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "允许延迟")
                    }
                    
                    Switch(
                        checked = alarm.snoozeEnabled,
                        onCheckedChange = { onAlarmChanged(alarm.copy(snoozeEnabled = it)) }
                    )
                }
                
                // 只有当允许延迟时才显示延迟设置
                if (alarm.snoozeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "延迟间隔: ${alarm.snoozeInterval} 分钟",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Slider(
                        value = alarm.snoozeInterval.toFloat(),
                        onValueChange = { onAlarmChanged(alarm.copy(snoozeInterval = it.toInt())) },
                        valueRange = 1f..30f,
                        steps = 29
                    )
                    
                    Text(
                        text = "延迟次数: ${alarm.snoozeTimes} 次",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Slider(
                        value = alarm.snoozeTimes.toFloat(),
                        onValueChange = { onAlarmChanged(alarm.copy(snoozeTimes = it.toInt())) },
                        valueRange = 1f..5f,
                        steps = 4
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 闹钟铃声设置
        RingtoneSelector(
            currentRingtoneUri = alarm.ringtoneUri,
            onRingtoneSelected = { onAlarmChanged(alarm.copy(ringtoneUri = it)) }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 小时和分钟选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时选择器
                    NumberPicker(
                        value = hour,
                        onValueChange = { hour = it },
                        range = 0..23,
                        formatter = { String.format("%02d", it) }
                    )
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // 分钟选择器
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        formatter = { String.format("%02d", it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(hour, minute) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    formatter: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = {
                val newValue = if (value + 1 > range.last) range.first else value + 1
                onValueChange(newValue)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "增加")
        }
        
        Text(
            text = formatter(value),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        IconButton(
            onClick = {
                val newValue = if (value - 1 < range.first) range.last else value - 1
                onValueChange(newValue)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "减少")
        }
    }
}

@Composable
fun RepeatDaysSelector(
    selectedDays: Set<Int>,
    onDaysSelected: (Set<Int>) -> Unit
) {
    val daysOfWeek = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index + 1)
            DayButton(
                day = day,
                isSelected = isSelected,
                onClick = {
                    val newSelectedDays = selectedDays.toMutableSet().apply {
                        if (isSelected) remove(index + 1) else add(index + 1)
                    }
                    onDaysSelected(newSelectedDays)
                }
            )
        }
    }
}

@Composable
fun DayButton(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = day.substring(1, 2),  // 只显示第二个字符，如"周一"显示"一"
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RingtoneSelector(
    currentRingtoneUri: String?,
    onRingtoneSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(
        context, RingtoneManager.TYPE_ALARM
    ).toString()
    
    val ringtoneOptions = remember {
        listOf(
            Triple(null, "系统默认", Icons.Default.NotificationsActive),
            Triple(defaultRingtone, "闹钟铃声", Icons.Default.Alarm),
            // 在实际应用中，这里可以添加更多铃声选项
        )
    }
    
    var selectedRingtoneUri by remember {
        mutableStateOf(currentRingtoneUri ?: defaultRingtone)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "闹钟铃声",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(modifier = Modifier.selectableGroup()) {
                ringtoneOptions.forEach { (uri, name, icon) ->
                    val isSelected = selectedRingtoneUri == uri
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    selectedRingtoneUri = uri
                                    onRingtoneSelected(uri)
                                }
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        RadioButton(
                            selected = isSelected,
                            onClick = null  // 点击事件已在Row中设置
                        )
                    }
                }
            }
        }
    }
}