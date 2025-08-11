package com.clockapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.clockapp.ui.main.MainScreen
import com.clockapp.ui.main.MainViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreenRoute()
        }
    }
}

@Composable
fun MainScreenRoute(
    viewModel: MainViewModel = hiltViewModel()
) {
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