package com.clockapp.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clockapp.data.model.Alarm
import com.clockapp.ui.alarm.AlarmForm
import java.util.*

@Composable
fun MainScreen(
    alarms: List<Alarm>,
    uiState: MainUiState,
    onAddAlarm: (Alarm) -> Unit,
    onUpdateAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onToggleAlarm: (Alarm) -> Unit,
    onSkipNextAlarm: (Alarm) -> Unit,
    onClearError: () -> Unit,
    onShowAddAlarmDialog: () -> Unit,
    onHideAddAlarmDialog: () -> Unit,
    onShowEditAlarmDialog: (Alarm) -> Unit,
    onHideEditAlarmDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("闹钟") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // 添加更多动作按钮，如设置等
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onShowAddAlarmDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加闹钟",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // 主界面内容
            if (alarms.isEmpty()) {
                // 空界面
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "未设置闹钟",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角的+按钮添加闹钟",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 闹钟列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(
                        items = alarms.sortedWith(compareBy({ !it.isEnabled }, { it.hour }, { it.minute })),
                        key = { it.id }
                    ) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { onToggleAlarm(alarm) },
                            onEdit = { onShowEditAlarmDialog(alarm) },
                            onSkip = { onSkipNextAlarm(alarm) },
                            onDelete = { onDeleteAlarm(alarm) }
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }
                }
            }
            
            // 错误提示
            ErrorMessageEffect(errorMessage = uiState.errorMessage, onClearError = onClearError)
            
            // 添加闹钟对话框
            if (uiState.showAddAlarmDialog) {
                AlarmDialog(
                    title = "添加闹钟",
                    confirmButtonText = "添加",
                    onDismiss = onHideAddAlarmDialog,
                    onConfirm = { alarm -> 
                        onAddAlarm(alarm)
                        onHideAddAlarmDialog()
                    }
                )
            }
            
            // 编辑闹钟对话框
            if (uiState.showEditAlarmDialog && uiState.editingAlarm != null) {
                AlarmDialog(
                    title = "编辑闹钟",
                    confirmButtonText = "更新",
                    initialAlarm = uiState.editingAlarm,
                    onDismiss = onHideEditAlarmDialog,
                    onConfirm = { alarm ->
                        onUpdateAlarm(alarm)
                        onHideEditAlarmDialog()
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 主视图
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 时间和信息
                Column {
                    Text(
                        text = alarm.getTimeString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (alarm.repeatDays.isNotEmpty()) {
                            Text(
                                text = alarm.getRepeatDaysString(),
                                fontSize = 14.sp,
                                color = if (alarm.isEnabled) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        } else {
                            Text(
                                text = "仅一次",
                                fontSize = 14.sp,
                                color = if (alarm.isEnabled) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        
                        if (alarm.label.isNotEmpty()) {
                            Text(
                                text = " • ${alarm.label}",
                                fontSize = 14.sp,
                                color = if (alarm.isEnabled) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // 开关
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                )
            }
            
            // 展开视图
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // 操作按钮
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OperationButton(
                            icon = Icons.Default.Edit,
                            text = "编辑",
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onEdit
                        )
                        
                        if (alarm.isEnabled) {
                            OperationButton(
                                icon = Icons.Default.SkipNext,
                                text = "跳过",
                                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                onClick = onSkip
                            )
                        }
                        
                        OperationButton(
                            icon = Icons.Default.Delete,
                            text = "删除",
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OperationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 12.sp)
    }
}

@Composable
fun ErrorMessageEffect(errorMessage: String?, onClearError: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearError()
        }
    }
    
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    )
}

@Composable
fun AlarmDialog(
    title: String,
    confirmButtonText: String,
    initialAlarm: Alarm? = null,
    onDismiss: () -> Unit,
    onConfirm: (Alarm) -> Unit
) {
    val defaultAlarm = initialAlarm ?: Alarm(
        id = 0,
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        minute = Calendar.getInstance().get(Calendar.MINUTE),
        isEnabled = true,
        label = "",
        repeatDays = setOf(),
        ringtoneUri = null,
        vibrate = true,
        snoozeEnabled = true,
        snoozeDuration = 10,
        volume = 50,
        createdAt = System.currentTimeMillis(),
        isOneTime = false,
        specificDate = null
    )
    
    var currentAlarm by remember { mutableStateOf(defaultAlarm) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            AlarmForm(
                alarm = currentAlarm,
                onAlarmChanged = { currentAlarm = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentAlarm) }
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "取消")
            }
        }
    )
}