package com.example.data

data class ActiveShipUpgrades(
    val hasHyperThrusters: Boolean = false,
    val hasPolarizedShields: Boolean = false,
    val hasDrillArmBuffer: Boolean = false,
    val hasLaserArrayBuffer: Boolean = false,
    val thrusterEfficiency: Float = 1.0f,
    val weaponChargeRate: Float = 1.0f,
    val shieldingFactor: Float = 1.0f
) {
    fun getSpeedBoostMultiplier(): Float {
        return if (hasHyperThrusters) 1.5f else 1.0f
    }

    fun getDefenseMultiplier(): Float {
        return if (hasPolarizedShields) 1.3f else 1.0f
    }
}
