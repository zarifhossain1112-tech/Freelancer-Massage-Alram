package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.db.GigAlarmDatabase
import com.example.data.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

data class ActiveAlarmData(
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AlarmSoundPlayer {
    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    private val _activeAlarm = MutableStateFlow<ActiveAlarmData?>(null)
    val activeAlarm: StateFlow<ActiveAlarmData?> = _activeAlarm.asStateFlow()

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var vibrator: Vibrator? = null
    private var soundJob: Job? = null
    private var timeoutJob: Job? = null
    private var previousVolume: Int? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startAlarm(
        context: Context,
        alarmData: ActiveAlarmData,
        settings: AppSettings,
        toneOverride: String? = null,
        volumeOverride: Int? = null,
        durationOverride: Int? = null,
        vibrationOverride: String? = null
    ) {
        stopAlarm(context)

        _activeAlarm.value = alarmData
        _isRinging.value = true

        val appContext = context.applicationContext

        // 1. Wake the screen
        try {
            val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "gigalarm:alert_wakelock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes max
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Volume management (Intensity)
        val targetVolumePercent = if (volumeOverride != null && volumeOverride in 1..100) {
            volumeOverride
        } else {
            settings.alarmVolume
        }

        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (settings.overrideSilentMode) {
            try {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                val targetVol = ((targetVolumePercent / 100.0) * maxVol).toInt().coerceIn(1, maxVol)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Vibration Pattern
        val effectiveVibration = if (!vibrationOverride.isNullOrBlank() && vibrationOverride != "DEFAULT") {
            vibrationOverride
        } else {
            settings.vibrationPattern
        }
        startVibration(appContext, effectiveVibration)

        // 4. Sound Tone playback (Predefined synthesized or Custom user-uploaded file)
        val effectiveTone = if (!toneOverride.isNullOrBlank() && toneOverride != "DEFAULT") {
            toneOverride
        } else {
            settings.alarmSound
        }
        startSoundTone(appContext, effectiveTone)

        // 5. Duration (Auto timeout)
        val effectiveDuration = if (durationOverride != null && durationOverride >= 0) {
            durationOverride
        } else {
            settings.ringDurationSeconds
        }

        if (effectiveDuration > 0) {
            timeoutJob = scope.launch {
                delay(effectiveDuration * 1000L)
                stopAlarm(appContext)
            }
        }
    }

    fun testVibrationPattern(context: Context, pattern: String) {
        val vibratorInst = getVibrator(context) ?: return
        if (pattern == "OFF" || !vibratorInst.hasVibrator()) return

        val timings = getVibrationTimings(pattern)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, -1)
            vibratorInst.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibratorInst.vibrate(timings, -1)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun getVibrationTimings(pattern: String): LongArray {
        return when (pattern) {
            "CONTINUOUS" -> longArrayOf(0, 1200, 200)
            "RAPID" -> longArrayOf(0, 150, 100, 150, 100, 150, 100, 150, 100)
            "SOS" -> longArrayOf(0, 200, 100, 200, 100, 200, 300, 500, 200, 500, 200, 500, 300)
            "HEARTBEAT" -> longArrayOf(0, 140, 120, 140, 700)
            "OFF" -> longArrayOf(0)
            else -> longArrayOf(0, 500, 250, 500, 250) // PULSE
        }
    }

    private fun startVibration(context: Context, pattern: String) {
        try {
            vibrator = getVibrator(context)
            if (pattern == "OFF" || vibrator?.hasVibrator() != true) return

            val timings = getVibrationTimings(pattern)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSoundTone(context: Context, toneType: String) {
        // A. Check if this is a custom uploaded sound
        if (toneType.startsWith("CUSTOM:") || toneType.startsWith("/")) {
            scope.launch(Dispatchers.IO) {
                val filePath = if (toneType.startsWith("CUSTOM:")) {
                    val soundId = toneType.removePrefix("CUSTOM:")
                    val sound = GigAlarmDatabase.getDatabase(context).customSoundDao().getSoundById(soundId)
                    sound?.filePath
                } else {
                    toneType
                }

                if (filePath != null && File(filePath).exists()) {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(filePath)
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            isLooping = true
                            prepare()
                            start()
                        }
                        return@launch
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // If custom audio file failed or was deleted, fallback to siren
                launch(Dispatchers.Default) {
                    synthesizeSound("SIREN")
                }
            }
            return
        }

        // B. Check if this is SYSTEM_ALARM
        if (toneType == "SYSTEM_ALARM") {
            try {
                var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // C. Synthesize audio dynamically using PCM AudioTrack
        synthesizeSound(toneType)
    }

    private fun synthesizeSound(toneType: String) {
        soundJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (sampleRate * 0.1).toInt().coerceAtLeast(minBufSize)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val buffer = ShortArray(sampleRate / 10)
            var phase = 0.0
            var timeMs = 0L

            try {
                while (isActive && _isRinging.value) {
                    val freq = when (toneType) {
                        "RADAR" -> {
                            val cycle = (timeMs % 400)
                            if (cycle < 200) 1400.0 else 0.0
                        }
                        "PULSE" -> {
                            val cycle = (timeMs % 240)
                            if (cycle < 120) 1050.0 else 0.0
                        }
                        "CASH_REGISTER" -> {
                            // Cha-Ching money register sound: 1318Hz -> 1760Hz with bright harmonics
                            val cycle = (timeMs % 800)
                            when {
                                cycle < 150 -> 1318.5
                                cycle in 150..400 -> 1760.0
                                else -> 0.0
                            }
                        }
                        "CHIME" -> {
                            val cycle = (timeMs % 600)
                            if (cycle < 300) 587.33 else 880.0
                        }
                        "DIGITAL_ALARM" -> {
                            // Classic electronic dual beeps
                            val cycle = (timeMs % 600)
                            when {
                                cycle < 120 -> 2048.0
                                cycle in 180..300 -> 2048.0
                                else -> 0.0
                            }
                        }
                        "SPACE_ALERT" -> {
                            // Sci-Fi downward sweep klaxon
                            val cycle = (timeMs % 700)
                            if (cycle < 450) {
                                1600.0 - (cycle / 450.0) * 800.0
                            } else {
                                0.0
                            }
                        }
                        else -> {
                            // "SIREN" emergency undulating siren from 750Hz to 1600Hz
                            val cycle = (timeMs % 1000) / 1000.0
                            800.0 + 800.0 * sin(cycle * Math.PI)
                        }
                    }

                    for (i in buffer.indices) {
                        if (freq > 0) {
                            val sample = (sin(phase) * Short.MAX_VALUE * 0.85).toInt().toShort()
                            buffer[i] = sample
                            phase += 2.0 * Math.PI * freq / sampleRate
                            if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        } else {
                            buffer[i] = 0
                        }
                    }

                    track.write(buffer, 0, buffer.size)
                    timeMs += (buffer.size * 1000L / sampleRate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun stopAlarm(context: Context) {
        _isRinging.value = false
        _activeAlarm.value = null

        timeoutJob?.cancel()
        timeoutJob = null

        soundJob?.cancel()
        soundJob = null

        try {
            audioTrack?.let {
                it.stop()
                it.release()
            }
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        previousVolume?.let { prev ->
            try {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.setStreamVolume(AudioManager.STREAM_ALARM, prev, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previousVolume = null
        }

        wakeLock?.let {
            if (it.isHeld) {
                try {
                    it.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            wakeLock = null
        }
    }

    fun snooze(context: Context, minutes: Int, settings: AppSettings) {
        val current = _activeAlarm.value
        stopAlarm(context)
        if (current != null && minutes > 0) {
            scope.launch {
                delay(minutes * 60 * 1000L)
                startAlarm(context, current, settings)
            }
        }
    }
}
