package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.data.model.AppSettings
import com.example.data.model.CustomSound
import com.example.data.model.PredefinedSoundsData
import com.example.service.ActiveAlarmData
import com.example.service.AlarmSoundPlayer
import com.example.service.NotificationPermissionHelper
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun SettingsTabScreen(
    settings: AppSettings,
    customSounds: List<CustomSound>,
    isRinging: Boolean,
    onUpdateSettings: (AppSettings) -> Unit,
    onImportCustomAudio: (Uri) -> Unit,
    onDeleteCustomSound: (CustomSound) -> Unit
) {
    val context = LocalContext.current
    var previewingToneKey by remember { mutableStateOf<String?>(null) }
    var soundTabSelected by remember { mutableIntStateOf(0) }

    // Launcher for selecting audio files without dangerous permissions
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportCustomAudio(uri)
        }
    }

    val vibrations = listOf(
        "PULSE" to "পালস ভাইব্রেশন (Pulsing)",
        "RAPID" to "তীব্র দ্রুত ভাইব্রেশন (Rapid Buzz)",
        "CONTINUOUS" to "একটানা ভারী (Continuous Heavy)",
        "SOS" to "এসওএস প্যাটার্ন (SOS Morse)",
        "HEARTBEAT" to "হার্টবিট ছন্দ (Heartbeat)",
        "OFF" to "ভাইব্রেশন বন্ধ (Off)"
    )

    val durations = listOf(
        15 to "১৫ সেকেন্ড",
        30 to "৩০ সেকেন্ড",
        60 to "১ মিনিট (প্রস্তাবিত)",
        120 to "২ মিনিট",
        300 to "৫ মিনিট",
        0 to "বন্ধ না করা পর্যন্ত"
    )

    fun playPreview(toneKey: String) {
        if (isRinging && previewingToneKey == toneKey) {
            AlarmSoundPlayer.stopAlarm(context)
            previewingToneKey = null
        } else {
            previewingToneKey = toneKey
            val testData = ActiveAlarmData(
                packageName = context.packageName,
                appName = "GigAlarm",
                title = "সাউন্ড প্রিভিউ",
                message = "অ্যালার্ম সাউন্ড টেস্ট চলছে..."
            )
            AlarmSoundPlayer.startAlarm(
                context = context,
                alarmData = testData,
                settings = settings,
                toneOverride = toneKey
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: Alarm Sound Tone (Predefined + Custom Uploads)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            modifier = Modifier.fillMaxWidth().testTag("sound_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অ্যালার্ম সাউন্ড ও রিংটোন",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    // Global Stop Button if ringing
                    if (isRinging) {
                        Button(
                            onClick = {
                                AlarmSoundPlayer.stopAlarm(context)
                                previewingToneKey = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AlarmRed),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("থামান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sound Tabs: Predefined List vs Custom Uploaded
                TabRow(
                    selectedTabIndex = soundTabSelected,
                    containerColor = Color(0xFF0F172A),
                    contentColor = EmeraldPrimary,
                    modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = soundTabSelected == 0,
                        onClick = { soundTabSelected = 0 },
                        text = {
                            Text("প্রিসেট তালিকা (${PredefinedSoundsData.SOUNDS.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    )
                    Tab(
                        selected = soundTabSelected == 1,
                        onClick = { soundTabSelected = 1 },
                        text = {
                            Text("কাস্টম আপলোড (${customSounds.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (soundTabSelected) {
                    0 -> {
                        // PREDEFINED LIST
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PredefinedSoundsData.SOUNDS.forEach { item ->
                                val isSelected = settings.alarmSound == item.key
                                val isCurrentlyPlaying = isRinging && previewingToneKey == item.key

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            onUpdateSettings(settings.copy(alarmSound = item.key))
                                            playPreview(item.key)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            onUpdateSettings(settings.copy(alarmSound = item.key))
                                            playPreview(item.key)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                                fontSize = 13.5.sp
                                            )
                                        }
                                        Text(
                                            text = item.subtitle,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Preview button
                                    IconButton(
                                        onClick = { playPreview(item.key) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentlyPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Preview",
                                            tint = if (isCurrentlyPlaying) AlarmRed else EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // CUSTOM UPLOADED SOUNDS
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Upload Button
                            Button(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("upload_audio_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color(0xFF022C22)
                                )
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "নিজের অডিও আপলোড করুন (MP3/WAV)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (customSounds.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("এখনও কোনো কাস্টম অডিও আপলোড করেননি", color = Color(0xFFCBD5E1), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("আপনার পছন্দের যেকোনো MP3 বা রিংটোন ফাইল যুক্ত করুন", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            } else {
                                customSounds.forEach { sound ->
                                    val soundKey = "CUSTOM:${sound.id}"
                                    val isSelected = settings.alarmSound == soundKey
                                    val isCurrentlyPlaying = isRinging && previewingToneKey == soundKey

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onUpdateSettings(settings.copy(alarmSound = soundKey))
                                                playPreview(soundKey)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                onUpdateSettings(settings.copy(alarmSound = soundKey))
                                                playPreview(soundKey)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sound.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${sound.fileSize / 1024} KB • কাস্টম অডিও",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.5.sp
                                            )
                                        }

                                        // Play preview
                                        IconButton(
                                            onClick = { playPreview(soundKey) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentlyPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Preview",
                                                tint = if (isCurrentlyPlaying) AlarmRed else EmeraldPrimary
                                            )
                                        }

                                        // Delete custom sound
                                        IconButton(
                                            onClick = { onDeleteCustomSound(sound) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: Volume & Intensity Control
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            modifier = Modifier.fillMaxWidth().testTag("volume_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যালার্মের তীব্রতা ও ভলিউম নিয়ন্ত্রণ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ভলিউম মাত্রা (Intensity)", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Text("${settings.alarmVolume}%", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = settings.alarmVolume.toFloat(),
                    onValueChange = { onUpdateSettings(settings.copy(alarmVolume = it.toInt())) },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("volume_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Silent mode & DND override
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সাইলেন্ট মোড ওভাররাইড (DND Bypass)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "ফোন সাইলেন্ট থাকলেও জরুরি মেসেজে জোরালো শব্দে অ্যালার্ম বাজবে",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.overrideSilentMode,
                        onCheckedChange = { onUpdateSettings(settings.copy(overrideSilentMode = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldPrimary,
                            checkedTrackColor = EmeraldDark
                        )
                    )
                }
            }
        }

        // SECTION 3: Vibration Patterns with Live Preview
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            modifier = Modifier.fillMaxWidth().testTag("vibration_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিফল্ট ভাইব্রেশন স্টাইল",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { AlarmSoundPlayer.testVibrationPattern(context, settings.vibrationPattern) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("test_global_vibration_button")
                    ) {
                        Text("ভাইব্রেশন টেস্ট", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                vibrations.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateSettings(settings.copy(vibrationPattern = key))
                                AlarmSoundPlayer.testVibrationPattern(context, key)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.vibrationPattern == key,
                            onClick = {
                                onUpdateSettings(settings.copy(vibrationPattern = key))
                                AlarmSoundPlayer.testVibrationPattern(context, key)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = if (settings.vibrationPattern == key) Color.White else Color(0xFF94A3B8),
                            fontWeight = if (settings.vibrationPattern == key) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        // SECTION 4: Ring Duration Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            modifier = Modifier.fillMaxWidth().testTag("duration_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যালার্ম বাজার স্থায়িত্ব (Duration)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                durations.forEach { (seconds, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateSettings(settings.copy(ringDurationSeconds = seconds)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.ringDurationSeconds == seconds,
                            onClick = { onUpdateSettings(settings.copy(ringDurationSeconds = seconds)) },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = if (settings.ringDurationSeconds == seconds) Color.White else Color(0xFF94A3B8),
                            fontWeight = if (settings.ringDurationSeconds == seconds) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        // System Permission & Play Store Compliance Info Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "প্লে স্টোর ও গোপনীয়তা নীতি (Play Store Ready)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• অ্যাপটি শুধুমাত্র ব্যবহারকারীর নির্বাচিত অ্যাপের নোটিফিকেশন প্রক্রিয়া করে।\n" +
                            "• কোনো ব্যক্তিগত ডাটা বা মেসেজ সার্ভারে প্রেরণ করা হয় না—সব তথ্য ১০০% আপনার ফোনে সুরক্ষিত থাকে।\n" +
                            "• অফিসিয়াল Android NotificationListenerService API ব্যবহারের ফলে এটি Google Play নীতিমালার সম্পূর্ণ অনুগামী।",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { NotificationPermissionHelper.openNotificationAccessSettings(context) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("অ্যান্ড্রয়েড নোটিফিকেশন পারমিশন সেটিংস খুলুন", color = Color(0xFF38BDF8), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
