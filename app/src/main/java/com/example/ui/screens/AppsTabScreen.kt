package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.MonitoredApp
import com.example.data.util.PresetAppsData
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AppsTabScreen(
    monitoredApps: List<MonitoredApp>,
    appSettings: AppSettings,
    customSounds: List<com.example.data.model.CustomSound> = emptyList(),
    onToggleMaster: (Boolean) -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onUpdateApp: (MonitoredApp) -> Unit,
    onDeleteApp: (MonitoredApp) -> Unit,
    onOpenAddAppSheet: () -> Unit,
    onTestAppAlarm: (MonitoredApp) -> Unit,
    onTestVibration: (String) -> Unit,
    onQuickAddPreset: (packageName: String, appName: String, keywords: String) -> Unit
) {
    var appToCustomize by remember { mutableStateOf<MonitoredApp?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Alert Switch Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (appSettings.masterEnabled) Color(0xFF13222B) else Color(0xFF1E293B)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (appSettings.masterEnabled)
                                listOf(EmeraldPrimary, Color(0xFF047857))
                            else
                                listOf(Color(0xFF475569), Color(0xFF334155))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("master_switch_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (appSettings.masterEnabled) EmeraldPrimary else Color(0xFF64748B),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (appSettings.masterEnabled) Color(0xFF022C22) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (appSettings.masterEnabled) "মাস্টার অ্যালার্ম সক্রিয় (ON)" else "অ্যালার্ম বন্ধ আছে (OFF)",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (appSettings.masterEnabled)
                                        "সব নির্বাচিত অ্যাপ নজরদারিতে আছে"
                                    else
                                        "কোনো নোটিফিকেশনে অ্যালার্ম বাজবে না",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = appSettings.masterEnabled,
                            onCheckedChange = onToggleMaster,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.testTag("master_alarm_switch")
                        )
                    }
                }
            }

            // Quick Add Preset Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "দ্রুত অ্যাপ যুক্ত করুন (Quick Add):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val monitoredSet = monitoredApps.map { it.packageName }.toSet()
                    val unmonitoredPresets = PresetAppsData.PRESETS.filter { !monitoredSet.contains(it.packageName) }

                    if (unmonitoredPresets.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(unmonitoredPresets) { preset ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF1E293B),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                                        )
                                    ),
                                    modifier = Modifier.clickable {
                                        onQuickAddPreset(preset.packageName, preset.appName, preset.defaultKeywords)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            preset.appName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Monitored Apps Heading & Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নজরদারিতে থাকা অ্যাপস (${monitoredApps.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "${monitoredApps.count { it.isEnabled }} টি সক্রিয়",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Monitored Apps List
            if (monitoredApps.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কোনো অ্যাপ যোগ করা হয়নি",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Fiverr, Upwork বা আপনার ফোনের যেকোনো অ্যাপ নজরদারিতে রাখতে নিচের বাটনে চাপুন।",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenAddAppSheet,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("অ্যাপ যুক্ত করুন", color = Color(0xFF022C22), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(monitoredApps, key = { it.packageName }) { app ->
                    MonitoredAppCard(
                        app = app,
                        onToggle = { onToggleApp(app.packageName, it) },
                        onCustomize = { appToCustomize = app },
                        onDelete = { onDeleteApp(app) },
                        onTestAlarm = { onTestAppAlarm(app) }
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Any App
        FloatingActionButton(
            onClick = onOpenAddAppSheet,
            containerColor = EmeraldPrimary,
            contentColor = Color(0xFF022C22),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_app_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add App")
                Spacer(modifier = Modifier.width(6.dp))
                Text("অ্যাপ যুক্ত করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // App Customization Dialog
    appToCustomize?.let { targetApp ->
        AppCustomizationDialog(
            app = targetApp,
            customSounds = customSounds,
            onDismiss = { appToCustomize = null },
            onSave = { updatedApp ->
                onUpdateApp(updatedApp)
                appToCustomize = null
            },
            onTestVibration = onTestVibration
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonitoredAppCard(
    app: MonitoredApp,
    onToggle: (Boolean) -> Unit,
    onCustomize: () -> Unit,
    onDelete: () -> Unit,
    onTestAlarm: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isEnabled) Color(0xFF131B2E) else Color(0xFF0F172A)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (app.isEnabled)
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                else
                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_card_${app.packageName.replace('.', '_')}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (app.isEnabled) EmeraldDark.copy(alpha = 0.5f) else Color(0xFF334155),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(2).uppercase(),
                            color = if (app.isEnabled) EmeraldPrimary else Color(0xFF94A3B8),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = app.appName,
                            fontWeight = FontWeight.Bold,
                            color = if (app.isEnabled) Color.White else Color(0xFF94A3B8),
                            fontSize = 15.sp
                        )
                        Text(
                            text = app.packageName,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Switch(
                    checked = app.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldPrimary,
                        checkedTrackColor = EmeraldDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges showing configured intensity, duration, and vibration!
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().clickable { onCustomize() }
            ) {
                // Keyword filter pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (app.keywords.isBlank()) "🔍 সব নোটিফিকেশন" else "🔍 ${app.keywords}",
                        color = if (app.keywords.isBlank()) EmeraldPrimary else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Custom Volume / Intensity Badge
                if (app.customVolume > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔊 তীব্রতা: ${app.customVolume}%",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Custom Duration Badge
                if (app.customDurationSeconds >= 0) {
                    val durText = when (app.customDurationSeconds) {
                        0 -> "স্থায়িত্ব: একটানা"
                        60 -> "স্থায়িত্ব: ১ মিনিট"
                        120 -> "স্থায়িত্ব: ২ মিনিট"
                        else -> "স্থায়িত্ব: ${app.customDurationSeconds}s"
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⏱️ $durText",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Custom Vibration Pattern Badge
                if (app.customVibration != "DEFAULT") {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📳 ${app.customVibration}",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Test Alarm button
                    OutlinedButton(
                        onClick = onTestAlarm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Alarm",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "টেস্ট অ্যালার্ম",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Customize Button (opens AppCustomizationDialog)
                    OutlinedButton(
                        onClick = onCustomize,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "কাস্টমাইজ",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
