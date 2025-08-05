package com.clockapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
// import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Handle permission request
            }
        }
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            ClockAppTheme {
                val navController = rememberNavController()
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            val viewModel = hiltViewModel<com.clockapp.ui.main.MainViewModel>()
                            val alarms by viewModel.alarms.collectAsState()
                            val uiState by viewModel.uiState.collectAsState()
                            
                            MainScreen(
                                alarms = alarms,
                                uiState = uiState,
                                onAddAlarm = viewModel::addAlarm,
                                onUpdateAlarm = viewModel::updateAlarm,
                                onDeleteAlarm = viewModel::deleteAlarm,
                                onToggleAlarm = viewModel::toggleAlarm,
                                onSkipNextAlarm = viewModel::skipNextAlarm,
                                onClearError = viewModel::clearError,
                                onShowAddAlarmDialog = viewModel::showAddAlarmDialog,
                                onHideAddAlarmDialog = viewModel::hideAddAlarmDialog,
                                onShowEditAlarmDialog = viewModel::showEditAlarmDialog,
                                onHideEditAlarmDialog = viewModel::hideEditAlarmDialog
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        // 检查并请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // 检查精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // 导航到允许精确闹钟的设置页面
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    requestExactAlarmSettingLauncher.launch(intent)
                }
            }
        }
        
        // 检查振动权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.VIBRATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.VIBRATE),
                REQUEST_VIBRATE_PERMISSION
            )
        }
    }
    
    companion object {
        private const val REQUEST_VIBRATE_PERMISSION = 100
    }
}