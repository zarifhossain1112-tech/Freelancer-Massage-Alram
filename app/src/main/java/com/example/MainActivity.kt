package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.GigAlarmDatabase
import com.example.data.model.AppSettingsManager
import com.example.service.NotificationPermissionHelper
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveAlarmBar
import com.example.ui.components.PermissionBanner
import com.example.ui.screens.AddAppBottomSheet
import com.example.ui.screens.AppsTabScreen
import com.example.ui.screens.HistoryTabScreen
import com.example.ui.screens.SettingsTabScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = GigAlarmDatabase.getDatabase(this)
        val settingsManager = AppSettingsManager(this)

        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(db, settingsManager) as T
                }
            }
        )[MainViewModel::class.java]

        viewModel.checkPermission(this)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GigAlarmMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.checkPermission(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigAlarmMainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddAppSheet by remember { mutableStateOf(false) }

    val monitoredApps by viewModel.monitoredApps.collectAsState()
    val notificationLogs by viewModel.notificationLogs.collectAsState()
    val customSounds by viewModel.customSounds.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val isRinging by viewModel.isRinging.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmeraldDark.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GigAlarm",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (isPermissionGranted) EmeraldPrimary else Color(0xFFF59E0B),
                                            CircleShape
                                        )
                                )
                            }
                            Text(
                                text = "ফ্রিল্যান্স নোটিফিকেশন অ্যালার্ম",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    // Global Test Alarm Action
                    Button(
                        onClick = { viewModel.triggerTestAlarm(context) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF022C22)
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("global_test_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Alarm",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("টেস্ট", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B101B),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                    label = { Text("অ্যাপস (${monitoredApps.size})", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_apps")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("সেটিংস", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("হিস্ট্রি", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner
            PermissionBanner(
                isGranted = isPermissionGranted,
                onGrantClick = { NotificationPermissionHelper.openNotificationAccessSettings(context) }
            )

            // Active Alarm Notice Bar
            ActiveAlarmBar(
                isRinging = isRinging,
                onStopClick = { viewModel.stopAlarm(context) }
            )

            // Content Tabs
            when (selectedTab) {
                0 -> {
                    AppsTabScreen(
                        monitoredApps = monitoredApps,
                        appSettings = appSettings,
                        customSounds = customSounds,
                        onToggleMaster = { viewModel.toggleMaster(it) },
                        onToggleApp = { pkg, enabled -> viewModel.toggleApp(pkg, enabled) },
                        onUpdateApp = { updatedApp -> viewModel.updateApp(updatedApp) },
                        onDeleteApp = { viewModel.deleteApp(it) },
                        onOpenAddAppSheet = {
                            viewModel.loadInstalledApps(context)
                            showAddAppSheet = true
                        },
                        onTestAppAlarm = { app -> viewModel.triggerTestAlarm(context, app) },
                        onTestVibration = { pattern -> viewModel.testVibration(context, pattern) },
                        onQuickAddPreset = { pkg, name, keywords ->
                            viewModel.addApp(pkg = pkg, appName = name, keywords = keywords)
                        }
                    )
                }

                1 -> {
                    SettingsTabScreen(
                        settings = appSettings,
                        customSounds = customSounds,
                        isRinging = isRinging,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onImportCustomAudio = { uri -> viewModel.importCustomAudio(context, uri) },
                        onDeleteCustomSound = { sound -> viewModel.deleteCustomSound(context, sound) }
                    )
                }

                2 -> {
                    HistoryTabScreen(
                        logs = notificationLogs,
                        onClearLogs = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }

    if (showAddAppSheet) {
        AddAppBottomSheet(
            installedApps = installedApps,
            isLoading = isLoadingApps,
            onDismiss = { showAddAppSheet = false },
            onAddApp = { pkg, name, keywords ->
                viewModel.addApp(pkg = pkg, appName = name, keywords = keywords)
            }
        )
    }
}
