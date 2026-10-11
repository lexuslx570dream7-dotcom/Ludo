package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playClick(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            playTone(880f, 40)
        }
    }

    fun playDiceRoll(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        if (vibrationEnabled) {
            vibrate(50)
        }
        if (!soundEnabled) return
        scope.launch {
            val frequencies = listOf(350f, 520f, 420f, 600f, 480f)
            for (f in frequencies) {
                playTone(f, 35)
                kotlinx.coroutines.delay(25)
            }
        }
    }

    fun playPawnMove(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            playTone(660f, 50)
        }
    }

    fun playCapture(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        if (vibrationEnabled) {
            vibrate(100)
        }
        if (!soundEnabled) return
        scope.launch {
            playTone(523.25f, 60)
            kotlinx.coroutines.delay(20)
            playTone(392f, 90)
        }
    }

    fun playMagic(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            val magicArpeggio = listOf(587.33f, 739.99f, 880.00f, 1174.66f)
            for (f in magicArpeggio) {
                playTone(f, 60)
                kotlinx.coroutines.delay(35)
            }
        }
    }

    fun playTurnAlert(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            playTone(987.77f, 70)
        }
    }

    fun playVictory(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        scope.launch {
            val victoryNotes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (note in victoryNotes) {
                playTone(note, 120)
                kotlinx.coroutines.delay(70)
            }
        }
    }

    fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun playTone(freqHz: Float, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = 1.0 - (i.toDouble() / numSamples)
                val value = (sin(2.0 * Math.PI * freqHz * t) * envelope * 24000).toInt()
                sample[i] = value.toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(sample, 0, sample.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {
        }
    }
}
