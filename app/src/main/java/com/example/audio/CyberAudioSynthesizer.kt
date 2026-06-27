package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object CyberAudioSynthesizer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("CyberAudioSynthesizer", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playClick() {
        playTone(ToneGenerator.TONE_PROP_BEEP, 40)
    }

    fun playSuccessFanfare() {
        Thread {
            playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 130)
            Thread.sleep(150)
            playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 130)
            Thread.sleep(150)
            playTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
        }.start()
    }

    fun playAlarm() {
        Thread {
            for (i in 1..4) {
                playTone(ToneGenerator.TONE_SUP_CONGESTION, 100)
                Thread.sleep(150)
            }
        }.start()
    }

    fun playImpact() {
        playTone(ToneGenerator.TONE_CDMA_LOW_L, 250)
    }

    fun playConnectionChirp() {
        Thread {
            playTone(ToneGenerator.TONE_CDMA_PIP, 30)
            Thread.sleep(50)
            playTone(ToneGenerator.TONE_CDMA_PIP, 30)
        }.start()
    }

    fun playShieldHum() {
        Thread {
            for (i in 1..3) {
                playTone(ToneGenerator.TONE_PROP_PROMPT, 50)
                Thread.sleep(70)
            }
        }.start()
    }

    fun playEngineRev() {
        Thread {
            playTone(ToneGenerator.TONE_SUP_DIAL, 80)
            Thread.sleep(100)
            playTone(ToneGenerator.TONE_SUP_CONFIRM, 120)
        }.start()
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.e("CyberAudioSynthesizer", "Error synthesizing sound tone", e)
        }
    }
}
