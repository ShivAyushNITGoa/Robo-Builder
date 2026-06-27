package com.example.ui

import com.example.audio.CyberAudioSynthesizer

object SoundManager {
    fun playClick() {
        CyberAudioSynthesizer.playClick()
    }

    fun playConnect() {
        CyberAudioSynthesizer.playConnectionChirp()
    }

    fun playEngineIgnition() {
        CyberAudioSynthesizer.playEngineRev()
    }

    fun playLaserShield() {
        CyberAudioSynthesizer.playShieldHum()
    }

    fun playMissionSuccess() {
        CyberAudioSynthesizer.playSuccessFanfare()
    }

    fun playExplosion() {
        CyberAudioSynthesizer.playImpact()
    }
}
