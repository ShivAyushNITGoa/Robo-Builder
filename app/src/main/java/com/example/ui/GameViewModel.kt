package com.example.ui

import android.app.Application
import kotlin.math.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    Menu,
    LevelSelect,
    Game,
    Customization,
    Achievements,
    DailyChallenge,
    CustomLevelSelect,
    CustomLevelPlay,
    CustomLevelEditor,
    SpaceshipTasks
}

enum class SlotType {
    Leg,
    LeftArm,
    RightArm,
    Utility
}

sealed class SimulationState {
    object Idle : SimulationState()
    object Running : SimulationState()
    data class Success(val stars: Int, val coinsEarned: Int, val message: String) : SimulationState()
    data class Failure(val failType: String, val message: String) : SimulationState()
}

// Spark particle helper
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: String,
    val life: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        // Populate DB on start
        viewModelScope.launch {
            repository.initializeGameIfNeeded()
        }
    }

    // Flows from repository
    val levelProgressList: StateFlow<List<LevelProgress>> = repository.allLevelProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val achievementsList: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and Level selection state
    var currentScreen by mutableStateOf(Screen.Menu)
        private set

    var selectedLevel: LevelDefinition? by mutableStateOf(null)
        private set

    // Part selection state
    var selectedLegs by mutableStateOf("Empty")
        private set
    var selectedLeftArm by mutableStateOf("Empty")
        private set
    var selectedRightArm by mutableStateOf("Empty")
        private set
    var selectedUtility by mutableStateOf("Empty")
        private set

    // Interactive 3D Model rotation angles (in radians/degrees)
    var modelRotationAngleX by mutableStateOf(-0.2f)
    var modelRotationAngleY by mutableStateOf(0.4f)

    // Robot Part Styles, Materials and Animations
    var headStyle by mutableStateOf("Standard Dome")
    var headMaterial by mutableStateOf("Chrome Metal")
    var torsoStyle by mutableStateOf("Fusion Frame")
    var torsoMaterial by mutableStateOf("Chrome Metal")
    var armsStyle by mutableStateOf("Standard Claw")
    var armsMaterial by mutableStateOf("Chrome Metal")
    var legsStyle by mutableStateOf("Standard Biped")
    var legsMaterial by mutableStateOf("Chrome Metal")
    var activeAnimation by mutableStateOf("Idle Float")
    var isAnimating by mutableStateOf(true)

    // Manual cockpit override modes
    var manualOverrideMode by mutableStateOf(false)
    var reactorPowerLevel by mutableStateOf(0.5f)
    var cameraShakeAmount by mutableStateOf(0f)

    // Live telemetry streams
    var liveMotorRpm by mutableStateOf(0f)
    var liveCoreHeat by mutableStateOf(25f)
    var liveBatteryCharge by mutableStateOf(100f)

    // Simulation states
    var simulationState: SimulationState by mutableStateOf(SimulationState.Idle)
        private set
    var simulationProgress by mutableStateOf(0f)
        private set
    var simulationLog by mutableStateOf("Awaiting commands...")
        private set

    // Live Particle effects for canvas
    val particles = mutableStateListOf<Particle>()

    private var simulationJob: Job? = null

    fun toggleManualOverride() {
        manualOverrideMode = !manualOverrideMode
        SoundManager.playClick()
        if (manualOverrideMode) {
            simulationLog = "Manual Cockpit Override ENGAGED. Adjust reactor dials."
            liveMotorRpm = reactorPowerLevel * 8000f
            liveCoreHeat = 30f + reactorPowerLevel * 60f
            liveBatteryCharge = 100f
        } else {
            resetSimulation()
        }
    }

    fun adjustReactorPower(power: Float) {
        reactorPowerLevel = power
        liveMotorRpm = power * 9200f
        liveCoreHeat = 25f + power * 85f
        if (power > 0.85f) {
            triggerCameraShake(0.8f)
        }
    }

    fun triggerCameraShake(amount: Float) {
        cameraShakeAmount = amount
    }

    fun tickCameraShake() {
        if (cameraShakeAmount > 0f) {
            cameraShakeAmount = (cameraShakeAmount - 0.05f).coerceAtLeast(0f)
        }
    }

    fun onScreenChanged(screen: Screen) {
        SoundManager.playClick()
        currentScreen = screen
        if (screen == Screen.Game) {
            resetSimulation()
        }
    }

    fun selectLevel(levelId: Int) {
        SoundManager.playClick()
        val def = LevelDefinitions.levels.find { it.id == levelId }
        selectedLevel = def
        resetParts()
        currentScreen = Screen.Game
    }

    fun selectPart(slot: SlotType, partName: String) {
        if (simulationState == SimulationState.Running) return
        SoundManager.playConnect()
        
        when (slot) {
            SlotType.Leg -> selectedLegs = partName
            SlotType.LeftArm -> selectedLeftArm = partName
            SlotType.RightArm -> selectedRightArm = partName
            SlotType.Utility -> selectedUtility = partName
        }
    }

    private fun resetParts() {
        selectedLegs = "Empty"
        selectedLeftArm = "Empty"
        selectedRightArm = "Empty"
        selectedUtility = "Empty"
        simulationState = SimulationState.Idle
        simulationProgress = 0f
        simulationLog = "Assemble parts to begin."
        particles.clear()
        manualOverrideMode = false
        cameraShakeAmount = 0f
        liveMotorRpm = 0f
        liveCoreHeat = 25f
        liveBatteryCharge = 100f
    }

    fun resetSimulation() {
        simulationJob?.cancel()
        simulationState = SimulationState.Idle
        simulationProgress = 0f
        simulationLog = "Awaiting commands..."
        particles.clear()
        cameraShakeAmount = 0f
        liveMotorRpm = 0f
        liveCoreHeat = 25f
        liveBatteryCharge = 100f
    }

    fun startSimulation() {
        if (selectedLevel == null) return
        if (simulationState == SimulationState.Running) return

        simulationJob?.cancel()
        simulationState = SimulationState.Running
        particles.clear()
        cameraShakeAmount = 0f

        SoundManager.playEngineIgnition()

        simulationJob = viewModelScope.launch {
            val level = selectedLevel!!
            var currentLog = "Robot initialized. Running self-diagnostic..."
            simulationLog = currentLog
            
            val maxRpm = when (selectedLegs) {
                "Jetpack" -> 8500f
                "Hover Engine" -> 7200f
                "Jump Springs" -> 5000f
                "Spider Legs" -> 4500f
                "Tank Tracks" -> 3500f
                else -> 4000f
            }

            delay(800)

            // Step 1: Ignite/Launch
            currentLog = "Igniting mobility systems: $selectedLegs..."
            simulationLog = currentLog
            
            // Start advancing progress
            val durationMs = 4500
            val steps = 45
            val delayPerStep = (durationMs / steps).toLong()

            for (i in 1..steps) {
                simulationProgress = i / steps.toFloat()
                
                // Tick camera shake decays
                tickCameraShake()

                // Telemetry simulation
                val activePowerFactor = 1f + (if (selectedLeftArm != "Empty") 0.2f else 0f) + (if (selectedRightArm != "Empty") 0.2f else 0f)
                liveMotorRpm = (simulationProgress * maxRpm * (0.85f + sin(simulationProgress * 40f) * 0.15f)).coerceIn(0f, maxRpm)
                
                val heatRiseRate = if (selectedUtility == "Cooling System") 0.25f else 0.85f
                liveCoreHeat = 25f + (i * heatRiseRate * activePowerFactor)
                
                val batteryDrainRate = if (selectedUtility == "Battery Pack" || selectedUtility == "Turbo Battery") 0.3f else 0.7f
                liveBatteryCharge = (100f - (i * batteryDrainRate * activePowerFactor)).coerceIn(0f, 100f)

                // Add exhaust particles if using Jetpack, Hover, or Wheels
                generateExhaustParticles()
                
                // Keep moving particle simulations
                updateParticles()

                // Update logs dynamically depending on progress
                when {
                    i == 10 -> {
                        simulationLog = "Leaving the assembly bay..."
                    }
                    i == 20 -> {
                        simulationLog = "Approaching the hazard zone: ${level.hazardType.uppercase()}..."
                        triggerCameraShake(0.3f)
                    }
                    i == 23 -> {
                        // Evaluate intermediate survival
                        val legsOk = level.acceptedLegs.contains(selectedLegs) || level.acceptedLegs.isEmpty()
                        val armsOk = isArmSetupValid(level)
                        val utilOk = isUtilitySetupValid(level)

                        if (!legsOk) {
                            // Leg failure
                            delay(400)
                            triggerFailure(level, "wrong_legs")
                            return@launch
                        } else if (!armsOk) {
                            // Arm failure
                            delay(400)
                            triggerFailure(level, "wrong_arms")
                            return@launch
                        } else if (!utilOk) {
                            // Utility failure
                            delay(400)
                            triggerFailure(level, "no_utility")
                            return@launch
                        } else {
                            simulationLog = "Safety check passed. Deploying active tools..."
                            if (level.hazardType == "laser" || level.hazardType == "cosmic_radiation" || level.hazardType == "electric_eel") {
                                SoundManager.playLaserShield()
                                triggerCameraShake(0.6f)
                            }
                        }
                    }
                    i == 30 -> {
                        simulationLog = "Activating tool components: ${selectedLeftArm}/${selectedRightArm}!"
                        if (selectedLeftArm == "Drill" || selectedRightArm == "Drill" || selectedLeftArm == "Hammer" || selectedRightArm == "Hammer") {
                            triggerCameraShake(0.5f)
                        }
                    }
                    i == 38 -> {
                        simulationLog = "Hazard cleared! Approaching docking pad..."
                    }
                }
                delay(delayPerStep)
            }

            // If we get here, it is a success!
            simulationProgress = 1f
            calculateSuccess(level)
        }
    }

    private fun isArmSetupValid(level: LevelDefinition): Boolean {
        // If no arm is required, empty is fine
        if (level.acceptedArms.contains("Empty") && selectedLeftArm == "Empty" && selectedRightArm == "Empty") {
            return true
        }
        
        // Check if either of the selected arms matches any accepted arm
        return level.acceptedArms.contains(selectedLeftArm) || level.acceptedArms.contains(selectedRightArm)
    }

    private fun isUtilitySetupValid(level: LevelDefinition): Boolean {
        if (level.acceptedUtilities.contains("Empty")) return true
        return level.acceptedUtilities.contains(selectedUtility)
    }

    private fun triggerFailure(level: LevelDefinition, primaryError: String) {
        simulationJob?.cancel()
        
        // Find specific fail message
        val finalFailType = when {
            selectedLegs == "Empty" && primaryError == "wrong_legs" -> "no_legs"
            selectedLeftArm == "Empty" && selectedRightArm == "Empty" && primaryError == "wrong_arms" -> "no_arms"
            selectedUtility == "Empty" && primaryError == "no_utility" -> "no_utility"
            else -> primaryError
        }

        val message = level.failureMessages[finalFailType] 
            ?: level.failureMessages.values.firstOrNull() 
            ?: "Critical component error! The robot broke apart."

        simulationState = SimulationState.Failure(finalFailType, message)
        simulationLog = "💥 MISSION FAILED: $message"
        
        // Trigger high impacts
        triggerCameraShake(2.5f)
        SoundManager.playExplosion()

        liveMotorRpm = 0f
        liveCoreHeat = 120f

        // Create an explosion of sparks on the canvas!
        triggerExplosion()

        // Increment failures achievement
        viewModelScope.launch {
            repository.incrementFailures()
        }
    }

    private suspend fun calculateSuccess(level: LevelDefinition) {
        // Calculate stars
        var stars = 1
        
        // Perfect Legs check
        val legsPerfect = selectedLegs == level.perfectLegs
        
        // Perfect Arms check (either left or right arm matches the perfect tool)
        val armPerfect = (selectedLeftArm == level.perfectArm || selectedRightArm == level.perfectArm) || 
                         (level.perfectArm == "Empty" && selectedLeftArm == "Empty" && selectedRightArm == "Empty")

        if (legsPerfect && armPerfect) {
            stars = 3
        } else if (legsPerfect || armPerfect) {
            stars = 2
        }

        // Trigger victory haptics
        triggerCameraShake(0.8f)
        SoundManager.playMissionSuccess()

        liveMotorRpm = 0f
        liveCoreHeat = 35f

        // Save progress & award coins
        val coinReward = repository.completeLevel(level.id, stars)

        simulationState = SimulationState.Success(stars, coinReward, level.successMessage)
        simulationLog = "🏆 MISSION SUCCESS! Stars earned: $stars ⭐. Reward: +$coinReward 🪙"
    }

    // Shop & Customization logic
    suspend fun purchaseSkin(itemId: String, cost: Int): Boolean {
        return repository.purchaseCosmetic(itemId, cost)
    }

    fun selectCosmetic(type: String, value: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            when (type) {
                "paint" -> repository.updateCustomization(value, profile.selectedEyes, profile.selectedHat)
                "eyes" -> repository.updateCustomization(profile.selectedPaint, value, profile.selectedHat)
                "hat" -> repository.updateCustomization(profile.selectedPaint, profile.selectedEyes, value)
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            repository.awardCoins(75)
            repository.completeLevel(1, 1) // Soft tick level progress
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            val database = GameDatabase.getDatabase(getApplication())
            database.clearAllTables()
            repository.initializeGameIfNeeded()
            resetParts()
            currentScreen = Screen.Menu
        }
    }

    // Particle logic for custom canvas simulation
    private fun generateExhaustParticles() {
        val px = 100f + simulationProgress * 550f
        val py = 250f + if (selectedLegs == "Jetpack" || selectedLegs == "Hover Engine") -15f else 15f
        
        val colors = when (selectedLegs) {
            "Jetpack" -> listOf("#FF9100", "#FF3D00", "#FFEA00") // Fire
            "Hover Engine" -> listOf("#00E5FF", "#00B0FF", "#E0F7FA") // Electric/Plasma
            "Wheels" -> listOf("#8D6E63", "#BCAAA4", "#D7CCC8") // Dust
            "Tank Tracks" -> listOf("#5D4037", "#8D6E63", "#A1887F") // Dirt Treads
            else -> listOf("#90A4AE", "#CFD8DC", "#ECEFF1") // Spark/steam
        }

        if (Math.random() < 0.4) {
            particles.add(
                Particle(
                    x = px - 25f,
                    y = py,
                    vx = -2f - (Math.random() * 4).toFloat(),
                    vy = (Math.random() * 2 - 1).toFloat(),
                    color = colors.random(),
                    life = 15 + (Math.random() * 10).toInt()
                )
            )
        }
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.life <= 0) {
                iterator.remove()
            } else {
                // Apply velocity & friction
                val updated = p.copy(
                    x = p.x + p.vx,
                    y = p.y + p.vy,
                    vy = p.vy + 0.1f, // light gravity
                    life = p.life - 1
                )
                // In place update isn't possible directly on mutableStateListOf item unless we replace it
                val idx = particles.indexOf(p)
                if (idx != -1) {
                    particles[idx] = updated
                }
            }
        }
    }

    private fun triggerExplosion() {
        val px = 100f + simulationProgress * 550f
        val py = 250f
        val colors = listOf("#FF1744", "#FF9100", "#FFEA00", "#212121")
        
        for (i in 0..35) {
            val angle = Math.random() * 2 * Math.PI
            val speed = 2f + Math.random() * 6f
            particles.add(
                Particle(
                    x = px,
                    y = py,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat(),
                    color = colors.random(),
                    life = 30 + (Math.random() * 20).toInt()
                )
            )
        }
    }

    // Custom Levels List Flow
    val customLevelsList: StateFlow<List<CustomLevel>> = repository.allCustomLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedCustomLevel: CustomLevel? by mutableStateOf(null)
        private set

    fun selectCustomLevel(id: Int) {
        viewModelScope.launch {
            val list = customLevelsList.value
            selectedCustomLevel = list.find { it.id == id }
            resetParts()
            currentScreen = Screen.CustomLevelPlay
        }
    }

    fun saveCustomLevel(level: CustomLevel) {
        viewModelScope.launch {
            repository.createCustomLevel(level)
            // Trigger refresh
            selectedCustomLevel = null
            currentScreen = Screen.CustomLevelSelect
        }
    }

    fun deleteCustomLevel(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomLevel(id)
            if (selectedCustomLevel?.id == id) {
                selectedCustomLevel = null
                currentScreen = Screen.CustomLevelSelect
            }
        }
    }

    fun getPartWeight(part: String): Int {
        return when (part) {
            "Wheels" -> 10
            "Tank Tracks" -> 25
            "Spider Legs" -> 15
            "Hover Engine" -> 20
            "Jump Springs" -> 8
            "Jetpack" -> 30
            "Grabber" -> 8
            "Magnet" -> 12
            "Hammer" -> 22
            "Drill" -> 18
            "Welding Torch" -> 10
            "Shield Arm" -> 15
            "Battery Pack" -> 20
            "Turbo Battery" -> 15
            "Cooling System" -> 12
            "Object Detector" -> 5
            "Heat Sensor" -> 4
            else -> 0
        }
    }

    fun getPartPower(part: String): Int {
        return when (part) {
            "Wheels" -> 5
            "Tank Tracks" -> 10
            "Spider Legs" -> 12
            "Hover Engine" -> 25
            "Jump Springs" -> 15
            "Jetpack" -> 35
            "Grabber" -> 8
            "Magnet" -> 15
            "Hammer" -> 18
            "Drill" -> 20
            "Welding Torch" -> 22
            "Shield Arm" -> 5
            "Battery Pack" -> -30
            "Turbo Battery" -> -50
            "Cooling System" -> 10
            "Object Detector" -> 8
            "Heat Sensor" -> 6
            else -> 0
        }
    }

    fun getCustomWeight(): Int {
        val baseWeight = getPartWeight(selectedLegs) + getPartWeight(selectedLeftArm) + getPartWeight(selectedRightArm) + getPartWeight(selectedUtility)
        val styleWeight = when (headStyle) {
            "Standard Dome" -> 0
            "Stealth Visor" -> -2
            "Retro Lens" -> 2
            "Monocular Laser" -> 4
            else -> 0
        } + when (headMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 5
            "Carbon Fiber" -> -4
            "Rusty Scrapyard" -> 8
            "Neon Grid" -> -2
            else -> 0
        } + when (torsoStyle) {
            "Fusion Frame" -> 0
            "Shielded Carapace" -> 15
            "Minimal Chassis" -> -10
            else -> 0
        } + when (torsoMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 12
            "Carbon Fiber" -> -8
            "Rusty Scrapyard" -> 15
            "Neon Grid" -> -4
            else -> 0
        } + when (armsMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 4
            "Carbon Fiber" -> -3
            "Rusty Scrapyard" -> 6
            "Neon Grid" -> -1
            else -> 0
        } + when (legsMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 6
            "Carbon Fiber" -> -5
            "Rusty Scrapyard" -> 9
            "Neon Grid" -> -2
            else -> 0
        }
        return (baseWeight + styleWeight).coerceAtLeast(5)
    }

    fun getCustomPower(): Int {
        val basePower = getPartPower(selectedLegs) + getPartPower(selectedLeftArm) + getPartPower(selectedRightArm) + getPartPower(selectedUtility)
        val stylePower = when (headStyle) {
            "Standard Dome" -> 0
            "Stealth Visor" -> 2
            "Retro Lens" -> 4
            "Monocular Laser" -> 8
            else -> 0
        } + when (headMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 4
            "Carbon Fiber" -> 0
            "Rusty Scrapyard" -> 2
            "Neon Grid" -> 12
            else -> 0
        } + when (torsoStyle) {
            "Fusion Frame" -> 0
            "Shielded Carapace" -> 5
            "Minimal Chassis" -> -4
            else -> 0
        } + when (torsoMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 8
            "Carbon Fiber" -> 0
            "Rusty Scrapyard" -> 1
            "Neon Grid" -> 15
            else -> 0
        } + when (armsMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 2
            "Carbon Fiber" -> 0
            "Rusty Scrapyard" -> 0
            "Neon Grid" -> 6
            else -> 0
        } + when (legsMaterial) {
            "Chrome Metal" -> 0
            "Golden Cyber" -> 3
            "Carbon Fiber" -> 0
            "Rusty Scrapyard" -> 1
            "Neon Grid" -> 8
            else -> 0
        }
        return basePower + stylePower
    }

    fun getCustomEfficiency(): Int {
        val totalWeight = getCustomWeight()
        val totalPower = getCustomPower()
        
        // Base starting efficiency
        var efficiency = 100f
        
        // Weight penalty
        efficiency -= totalWeight * 0.4f
        
        // Power penalty/bonus (positive power consumes energy, negative power supplies energy)
        if (totalPower > 0) {
            efficiency -= totalPower * 0.5f
        } else {
            // Batteries actually generate surplus power, increasing efficiency!
            efficiency += -totalPower * 0.3f
        }
        
        // Material penalties/bonuses
        val materials = listOf(headMaterial, torsoMaterial, armsMaterial, legsMaterial)
        materials.forEach { mat ->
            when (mat) {
                "Rusty Scrapyard" -> efficiency -= 4f
                "Carbon Fiber" -> efficiency += 3f
                "Neon Grid" -> efficiency += 2f
                "Golden Cyber" -> efficiency -= 1f
            }
        }
        
        return efficiency.coerceIn(10f, 100f).toInt()
    }

    fun startCustomSimulation() {
        val level = selectedCustomLevel ?: return
        if (simulationState == SimulationState.Running) return

        simulationJob?.cancel()
        simulationState = SimulationState.Running
        particles.clear()

        simulationJob = viewModelScope.launch {
            var currentLog = "Assembled Custom Robot. Performing hardware scan..."
            simulationLog = currentLog
            delay(1000)

            // 1. Check Weights & Power limits
            val totalWeight = getPartWeight(selectedLegs) + getPartWeight(selectedLeftArm) + getPartWeight(selectedRightArm) + getPartWeight(selectedUtility)
            val totalPower = getPartPower(selectedLegs) + getPartPower(selectedLeftArm) + getPartPower(selectedRightArm) + getPartPower(selectedUtility)

            if (totalWeight > level.maxWeight) {
                simulationState = SimulationState.Failure("weight_limit", "Structural weight of ${totalWeight}kg exceeds sector load capacity of ${level.maxWeight}kg!")
                simulationLog = "💥 STRUCTURAL COLLAPSE: Chassis collapsed under its own weight!"
                triggerExplosion()
                return@launch
            }

            if (totalPower > level.maxEnergyCost) {
                simulationState = SimulationState.Failure("energy_limit", "Total energy cost is ${totalPower} Units, exceeding limits of ${level.maxEnergyCost} Units!")
                simulationLog = "💥 POWER OVERLOAD: Electrical wiring caught fire from extreme power draw!"
                triggerExplosion()
                return@launch
            }

            currentLog = "Hardware scan PASSED! Weight: ${totalWeight}kg, Power: ${totalPower} Units. Launching..."
            simulationLog = currentLog
            delay(800)

            // Run progress timeline
            val steps = 50
            val delayPerStep = 90L // 4.5 seconds total

            for (i in 1..steps) {
                simulationProgress = i / steps.toFloat()
                generateExhaustParticles()
                updateParticles()

                val currentProgPercent = simulationProgress

                // Evaluate custom obstacles
                val obstacles = level.obstaclesLayout.split(",").filter { it.contains(":") }
                for (obs in obstacles) {
                    val split = obs.split(":")
                    val type = split[0]
                    val obsPos = split[1].toFloatOrNull() ?: 0.5f

                    if (kotlin.math.abs(currentProgPercent - obsPos) < 0.02f) {
                        val isProtected = when (type) {
                            "lava" -> selectedUtility == "Cooling System" || selectedLegs == "Jetpack"
                            "river" -> selectedLegs == "Hover Engine" || selectedLegs == "Jetpack" || selectedLegs == "Jump Springs"
                            "quicksand" -> selectedLegs == "Spider Legs" || selectedLegs == "Hover Engine" || selectedLegs == "Jetpack"
                            "laser" -> selectedLeftArm == "Shield Arm" || selectedRightArm == "Shield Arm" || selectedUtility == "Cooling System"
                            "wall" -> selectedLeftArm == "Hammer" || selectedRightArm == "Hammer" || selectedLeftArm == "Drill" || selectedRightArm == "Drill" || selectedLeftArm == "Welding Torch" || selectedRightArm == "Welding Torch"
                            else -> true
                        }

                        if (!isProtected) {
                            delay(400)
                            val failMsg = when (type) {
                                "lava" -> "The intense lava melted your robot's wheels. Equip Cooling System or Jetpack!"
                                "river" -> "The robot fell into the deep river. Equip Hover Engine or Jetpack!"
                                "quicksand" -> "The robot got stuck in quicksand. Equip Spider Legs or Hover Engine!"
                                "laser" -> "The solar security laser melted your CPU. Equip Shield Arm!"
                                "wall" -> "The robot crashed face-first into the concrete barrier. Equip Hammer, Drill, or Torch!"
                                else -> "Robot crashed into custom obstacle!"
                            }
                            simulationState = SimulationState.Failure(type, failMsg)
                            simulationLog = "💥 DESTROYED BY ${type.uppercase()}: $failMsg"
                            triggerExplosion()
                            return@launch
                        } else {
                            simulationLog = "Successfully bypassed hazard: ${type.uppercase()}!"
                        }
                    }
                }

                if (i == 10) simulationLog = "Starting navigation sequence..."
                if (i == 25) simulationLog = "Halfway through custom test area..."
                if (i == 40) simulationLog = "Approaching final custom exit pad..."

                delay(delayPerStep)
            }

            simulationProgress = 1f
            
            // Format of objectiveGoals: "Goal1|Desc1|Type:Val;Goal2|Desc2|Type:Val"
            val goals = level.objectiveGoals.split(";").filter { it.contains("|") }
            var goalsMet = 0
            val resultsSummary = StringBuilder()

            for (g in goals) {
                val split = g.split("|")
                if (split.size >= 3) {
                    val goalName = split[0]
                    val goalRule = split[2]

                    var isGoalMet = false
                    val ruleSplit = goalRule.split(":")
                    if (ruleSplit.size >= 2) {
                        val checkType = ruleSplit[0]
                        val checkParams = ruleSplit[1].split(",")

                        isGoalMet = when (checkType) {
                            "LEG" -> checkParams.contains(selectedLegs)
                            "ARM" -> checkParams.contains(selectedLeftArm) || checkParams.contains(selectedRightArm)
                            "UTIL" -> checkParams.contains(selectedUtility)
                            "WEIGHT" -> {
                                val maxVal = checkParams.firstOrNull()?.toIntOrNull() ?: 100
                                totalWeight <= maxVal
                            }
                            "ENERGY" -> {
                                val maxVal = checkParams.firstOrNull()?.toIntOrNull() ?: 100
                                totalPower <= maxVal
                            }
                            else -> true
                        }
                    }

                    if (isGoalMet) {
                        goalsMet++
                        resultsSummary.append("✅ $goalName met!\n")
                    } else {
                        resultsSummary.append("❌ $goalName unmet.\n")
                    }
                }
            }

            val totalGoals = goals.size.coerceAtLeast(1)
            if (goalsMet == 0) {
                simulationState = SimulationState.Failure("objectives_failed", "Robot completed traversal but failed all objective goals:\n$resultsSummary")
                simulationLog = "💥 MISSION FAILURE: All custom objectives failed!"
                triggerExplosion()
            } else {
                val starsEarned = when {
                    goalsMet == totalGoals -> 3
                    goalsMet >= totalGoals / 2.0 -> 2
                    else -> 1
                }

                repository.completeCustomLevel(level.id, starsEarned)
                simulationState = SimulationState.Success(starsEarned, 50, "You cleared the custom level with $goalsMet/$totalGoals goals accomplished!\n\n$resultsSummary")
                simulationLog = "🏆 CUSTOM PUZZLE CLEARED: Stars: $starsEarned ⭐. Reward: +50 coins!"
            }
        }
    }

    fun awardCustomCoins(amount: Int) {
        viewModelScope.launch {
            repository.awardCoins(amount)
        }
    }
}
