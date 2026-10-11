package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.CustomSound
import com.example.data.model.MonitoredApp
import com.example.data.model.PredefinedSoundsData
import com.example.data.util.InstalledAppItem
import com.example.data.util.PresetAppsData
import com.example.service.AlarmSoundPlayer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppBottomSheet(
    installedApps: List<InstalledAppItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddApp: (packageName: String, appName: String, keywords: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var customPackageName by remember { mutableStateOf("") }
    var customAppName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131B2E),
        modifier = Modifier.testTag("add_app_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "নতুন অ্যাপ যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = EmeraldPrimary,
                modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ইনস্টল করা অ্যাপস", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("জনপ্রিয় প্রিসেট", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("ম্যানুয়াল", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_apps_input"),
                        placeholder = { Text("অ্যাপ খুঁজুন... (Search apps)", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = EmeraldPrimary)
                        }
                    } else {
                        val filtered = installedApps.filter {
                            it.appName.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("কোনো অ্যাপ পাওয়া যায়নি", color = Color(0xFF94A3B8))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filtered, key = { it.packageName }) { appItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(EmeraldDark.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = appItem.appName.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appItem.appName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = appItem.packageName,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }

                                        if (appItem.isAlreadyMonitored) {
                                            Text(
                                                text = "যুক্ত আছে",
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            Button(
                                                onClick = {
                                                    onAddApp(appItem.packageName, appItem.appName, "")
                                                    onDismiss()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text("যোগ", fontWeight = FontWeight.Bold, color = Color(0xFF022C22))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(PresetAppsData.PRESETS, key = { it.packageName }) { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(EmeraldDark.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset.appName.take(2).uppercase(),
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.appName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF334155), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(preset.category, fontSize = 9.sp, color = Color(0xFFE2E8F0))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.5.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        onAddApp(preset.packageName, preset.appName, preset.defaultKeywords)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF022C22))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("যুক্ত করুন", fontWeight = FontWeight.Bold, color = Color(0xFF022C22), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = customAppName,
                            onValueChange = { customAppName = it },
                            label = { Text("অ্যাপের নাম (App Name)") },
                            placeholder = { Text("যেমন: My Client App") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = { customPackageName = it },
                            label = { Text("প্যাকেজ নাম (Package Name)") },
                            placeholder = { Text("যেমন: com.client.customapp") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (customAppName.isNotBlank() && customPackageName.isNotBlank()) {
                                    onAddApp(customPackageName.trim(), customAppName.trim(), "")
                                    onDismiss()
                                }
                            },
                            enabled = customAppName.isNotBlank() && customPackageName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("অ্যাপ যুক্ত করুন", fontWeight = FontWeight.Bold, color = Color(0xFF022C22))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed customization dialog for an individual app:
 * - Keyword filtering
 * - Intensity (Volume level override)
 * - Duration of alarm (seconds override)
 * - Customized Vibration pattern (Pulse, Rapid, Continuous, SOS, Heartbeat, Off)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppCustomizationDialog(
    app: MonitoredApp,
    customSounds: List<CustomSound> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (MonitoredApp) -> Unit,
    onTestVibration: (String) -> Unit
) {
    var keywordsText by remember { mutableStateOf(app.keywords) }
    var allNotificationsMode by remember { mutableStateOf(app.keywords.isBlank()) }

    var useCustomVolume by remember { mutableStateOf(app.customVolume > 0) }
    var volumeLevel by remember { mutableIntStateOf(if (app.customVolume > 0) app.customVolume else 100) }

    var useCustomDuration by remember { mutableStateOf(app.customDurationSeconds >= 0) }
    var durationSeconds by remember { mutableIntStateOf(if (app.customDurationSeconds >= 0) app.customDurationSeconds else 60) }

    var vibrationPattern by remember { mutableStateOf(app.customVibration) }
    var soundTone by remember { mutableStateOf(app.customSound) }

    val presetTags = listOf(
        "order", "message", "inbox", "buyer", "urgent", "client",
        "offer", "interview", "proposal", "contract", "hire", "payment"
    )

    val vibrationOptions = listOf(
        "DEFAULT" to "গ্লোবাল ডিফল্ট (Default)",
        "PULSE" to "পালস ভাইব্রেশন (Pulsing)",
        "RAPID" to "তীব্র দ্রুত ভাইব্রেশন (Rapid Buzz)",
        "CONTINUOUS" to "একটানা ভারী (Continuous Heavy)",
        "SOS" to "এসওএস প্যাটার্ন (SOS Morse)",
        "HEARTBEAT" to "হার্টবিট ছন্দ (Heartbeat)",
        "OFF" to "ভাইব্রেশন বন্ধ (Mute)"
    )

    val durationOptions = listOf(
        15 to "১৫ সেকেন্ড",
        30 to "৩০ সেকেন্ড",
        60 to "১ মিনিট",
        120 to "২ মিনিট",
        300 to "৫ মিনিট",
        0 to "বন্ধ না করা পর্যন্ত"
    )

    val soundOptions = buildList {
        add("DEFAULT" to "গ্লোবাল ডিফল্ট টোন")
        PredefinedSoundsData.SOUNDS.forEach {
            add(it.key to "${it.iconEmoji} ${it.title}")
        }
        customSounds.forEach {
            add("CUSTOM:${it.id}" to "🎵 ${it.name}")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131B2E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(EmeraldDark.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.appName.take(1).uppercase(), color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${app.appName} কাস্টমাইজেশন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "অ্যালার্ম তীব্রতা, স্থায়িত্ব ও ভাইব্রেশন",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: Intensity / Volume
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অ্যালার্ম তীব্রতা (Volume)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        Switch(
                            checked = useCustomVolume,
                            onCheckedChange = { useCustomVolume = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldDark
                            )
                        )
                    }

                    if (useCustomVolume) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("কাস্টম তীব্রতা:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$volumeLevel%", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = volumeLevel.toFloat(),
                            onValueChange = { volumeLevel = it.toInt() },
                            valueRange = 10f..100f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldPrimary,
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )
                    } else {
                        Text(
                            text = "গ্লোবাল ভলিউম ব্যবহার হবে (ডিফল্ট)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // SECTION 2: Duration of Alarm
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বাজার স্থায়িত্ব (Duration)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        Switch(
                            checked = useCustomDuration,
                            onCheckedChange = { useCustomDuration = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldDark
                            )
                        )
                    }

                    if (useCustomDuration) {
                        Spacer(modifier = Modifier.height(6.dp))
                        durationOptions.forEach { (sec, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { durationSeconds = sec }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = durationSeconds == sec,
                                    onClick = { durationSeconds = sec },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    color = if (durationSeconds == sec) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (durationSeconds == sec) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "গ্লোবাল সময়কাল ব্যবহার হবে (ডিফল্ট)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // SECTION 3: Custom Vibration Patterns for this app!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("কাস্টম ভাইব্রেশন প্যাটার্ন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        // Test vibration button
                        OutlinedButton(
                            onClick = { onTestVibration(vibrationPattern) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("ভাইব্রেট টেস্ট", fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    vibrationOptions.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vibrationPattern = key
                                    onTestVibration(key)
                                }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = vibrationPattern == key,
                                onClick = {
                                    vibrationPattern = key
                                    onTestVibration(key)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (vibrationPattern == key) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (vibrationPattern == key) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // SECTION 4: Sound Tone Override
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অ্যালার্ম রিংটোন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    soundOptions.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { soundTone = key }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = soundTone == key,
                                onClick = { soundTone = key },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (soundTone == key) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (soundTone == key) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // SECTION 5: Keyword Filters
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("সব নোটিফিকেশনে বাজবে", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                        Switch(
                            checked = allNotificationsMode,
                            onCheckedChange = {
                                allNotificationsMode = it
                                if (it) keywordsText = ""
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldDark
                            )
                        )
                    }

                    if (!allNotificationsMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = keywordsText,
                            onValueChange = { keywordsText = it },
                            placeholder = { Text("order, message, inbox, urgent", color = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetTags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .clickable {
                                            val currentList = keywordsText
                                                .split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .toMutableList()
                                            if (!currentList.contains(tag)) {
                                                currentList.add(tag)
                                                keywordsText = currentList.joinToString(", ")
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+ $tag", fontSize = 10.5.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalKeywords = if (allNotificationsMode) "" else keywordsText.trim()
                    val finalVol = if (useCustomVolume) volumeLevel else -1
                    val finalDur = if (useCustomDuration) durationSeconds else -1

                    val updated = app.copy(
                        keywords = finalKeywords,
                        customVolume = finalVol,
                        customDurationSeconds = finalDur,
                        customVibration = vibrationPattern,
                        customSound = soundTone
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("সংরক্ষণ করুন (Save)", color = Color(0xFF022C22), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Color(0xFF94A3B8))
            }
        }
    )
}
