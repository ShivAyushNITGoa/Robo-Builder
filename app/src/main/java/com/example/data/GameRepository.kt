package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val allLevelProgress: Flow<List<LevelProgress>> = gameDao.getAllLevelProgress()
    val userProfile: Flow<UserProfile?> = gameDao.getUserProfile()
    val allAchievements: Flow<List<Achievement>> = gameDao.getAllAchievements()

    suspend fun getLevelProgressById(levelId: Int): LevelProgress? {
        return gameDao.getLevelProgressById(levelId)
    }

    suspend fun initializeGameIfNeeded() {
        // Initialize User Profile
        val currentProfile = gameDao.getUserProfileDirect()
        if (currentProfile == null) {
            gameDao.insertUserProfile(UserProfile())
        }

        // Initialize default custom levels
        val customLevelsList = gameDao.getAllCustomLevels().firstOrNull()
        if (customLevelsList.isNullOrEmpty()) {
            val defaultCustom1 = CustomLevel(
                title = "Acid Canyon Crossfire",
                description = "Navigate through a toxic river and concrete wall. Multiple solutions exist!",
                authorName = "LevelDesigner_99",
                difficulty = "Hard",
                allowedLegs = "Hover Engine,Jetpack,Spider Legs,Tank Tracks",
                allowedArms = "Empty,Hammer,Drill,Welding Torch,Shield Arm",
                allowedUtilities = "Empty,Cooling System,Turbo Battery",
                maxWeight = 50,
                maxEnergyCost = 80,
                batteryCapacity = 1200,
                obstaclesLayout = "river:0.3,wall:0.7",
                objectiveGoals = "Cross Acid Flow|Levitate above the toxic pool|LEG:Hover Engine,Jetpack;Demolish Wall|Shatter or melt the concrete block|ARM:Hammer,Drill,Welding Torch",
                completed = false,
                starsEarned = 0
            )
            val defaultCustom2 = CustomLevel(
                title = "Thermal Laser Facility",
                description = "High-density laser traps. Pass fast or build physical/energy deflectors.",
                authorName = "GamerOne",
                difficulty = "Expert",
                allowedLegs = "Wheels,Tank Tracks,Hover Engine,Jetpack",
                allowedArms = "Empty,Shield Arm,Welding Torch",
                allowedUtilities = "Empty,Cooling System,Turbo Battery,Battery Pack",
                maxWeight = 55,
                maxEnergyCost = 95,
                batteryCapacity = 1000,
                obstaclesLayout = "laser:0.5",
                objectiveGoals = "Deflect Lasers|Equip defense shield or cooling system|ARM:Shield Arm,UTIL:Cooling System;Efficiency Check|Assemble light configuration to save energy|WEIGHT:35",
                completed = false,
                starsEarned = 0
            )
            gameDao.insertCustomLevel(defaultCustom1)
            gameDao.insertCustomLevel(defaultCustom2)
        }

        // Initialize Achievements
        val defaultAchievements = listOf(
            Achievement("first_build", "First Robot", "Build and launch your very first robot!", false, 0, 1, 50),
            Achievement("star_collector", "Superstar", "Earn a total of 15 stars across levels.", false, 0, 15, 150),
            Achievement("coins_hoarder", "Golden Gear", "Collect 500 gold coins.", false, 0, 500, 100),
            Achievement("failure_guru", "Funny Failures", "Witness 5 spectacular robot crashes.", false, 0, 5, 80),
            Achievement("cosmetic_king", "Stylish Bot", "Unlock 3 custom robot cosmetic skins.", false, 0, 3, 120),
            Achievement("world_conqueror", "World Conqueror", "Complete all levels in World 1 (Factory).", false, 0, 3, 100)
        )
        gameDao.insertAllAchievements(defaultAchievements)

        // Initialize Levels
        val defaultLevels = listOf(
            // World 1: Factory
            LevelProgress(1, 1, 1, "Simple Bridge Crossing", "Traversal: Connect Wheels to traverse the flat factory floor safely.", 0, false),
            LevelProgress(2, 1, 2, "Crate Transporter", "Pick & Carry: Choose Grabber Arm + Wheels to deliver the microchip crate.", 0, false),
            LevelProgress(3, 1, 3, "Breaker of Walls", "Demolition: Demolish the heavy concrete barricade with a Hammer Arm.", 0, false),
            // World 2: Forest
            LevelProgress(4, 2, 1, "Deep River Crossing", "Aquatic: Cross the wide river! Normal wheels sink; use Hover or Jetpack.", 0, false),
            LevelProgress(5, 2, 2, "Forest Scrap Grab", "Magnetic: Retrieve the iron coin from the ditch using a Magnet Arm.", 0, false),
            LevelProgress(6, 2, 3, "Kangaroo Leap", "Acrobat: Leap over the dense thorn bushes. Equip Jump Springs!", 0, false),
            // World 3: Desert
            LevelProgress(7, 3, 1, "Sinking Quicksand", "Terrain: Quicksand swallows wheels. Equip Spider Legs or Hover Engine.", 0, false),
            LevelProgress(8, 3, 2, "Desert Laser Shield", "Defense: Block thermal security lasers using the Shield Arm.", 0, false),
            LevelProgress(9, 3, 3, "Sandstorm Search", "Sensory: Find the antenna in a sandstorm using the Object Detector.", 0, false),
            // World 4: Snow
            LevelProgress(10, 4, 1, "Slippery Ice Track", "Ice: Standard wheels skid. Use heavy Tank Tracks to stay stable.", 0, false),
            LevelProgress(11, 4, 2, "Frozen Wall Melt", "Thermal: Melt the giant glaciers blocking the research bunker using a Torch.", 0, false),
            LevelProgress(12, 4, 3, "Polar Explorer", "Arctic: Navigate frozen crevasses. Equip Jump Springs and Tank Tracks.", 0, false),
            // World 5: Volcano
            LevelProgress(13, 5, 1, "Lava Lake Overflight", "Volcanic: Fly over bubbling magma safely. Equip Jetpack or Hover.", 0, false),
            LevelProgress(14, 5, 2, "Magma Reactor Repair", "Hazard: Repair thermal piping with a Welding Torch & Cooling System.", 0, false),
            LevelProgress(15, 5, 3, "Volcanic Escape", "Super: Escape the melting factory! Choose Jetpack + Shield + Cooling.", 0, false),
            // World 6: Deep Space
            LevelProgress(16, 6, 1, "Asteroid Belt Clearance", "Demolition: Pulverize floating meteor obstacles using the Drill Arm.", 0, false),
            LevelProgress(17, 6, 2, "Cosmic Radiation Storm", "Defense: Block thermal stellar flare streams using the Shield Arm.", 0, false),
            LevelProgress(18, 6, 3, "Zero-G Void Leap", "Mobility: Navigate a complete gravitational vacuum using the Jetpack.", 0, false),
            // World 7: Cyber City
            LevelProgress(19, 7, 1, "Grid Lock Override", "Mainframe: Deactivate high-voltage grid barriers with the Shield Arm.", 0, false),
            LevelProgress(20, 7, 2, "Neon EMP Pulse Tower", "Resilience: Absorb electromagnetic tower pulses using Turbo Battery.", 0, false),
            LevelProgress(21, 7, 3, "Nanite Swarm Zone", "Thermal: Incinerate metal-devouring micro-nanites with a Welding Torch.", 0, false),
            // World 8: Ocean Depths
            LevelProgress(22, 8, 1, "Abyssal Pressure Trench", "Abyssal: Survive intense hydrostatic ocean pressures with Tank Tracks.", 0, false),
            LevelProgress(23, 8, 2, "Turbulent Tide Currents", "Aquatic: Anchor yourself to the rocky ocean bed with Spider Legs.", 0, false),
            LevelProgress(24, 8, 3, "Electric Eel Nest", "Grounded: Shield your delicate circuits from high-voltage eel discharges.", 0, false),
            LevelProgress(25, 8, 4, "Deep Sea Core Retrieve", "Submarine: Recover the titanium core from a deep trench using Magnet Arm.", 0, false)
        )
        gameDao.insertAllLevels(defaultLevels)
    }

    suspend fun completeLevel(levelId: Int, starsEarned: Int): Int {
        val currentProgress = gameDao.getLevelProgressById(levelId)
        val previouslyEarned = currentProgress?.stars ?: 0
        
        // Only update if stars earned is higher
        if (starsEarned > previouslyEarned) {
            if (currentProgress == null) {
                // Defensive insertion if the level was not seeded in database
                val levelDef = LevelDefinitions.levels.find { it.id == levelId }
                if (levelDef != null) {
                    gameDao.insertLevelProgress(
                        LevelProgress(
                            levelId = levelId,
                            worldId = levelDef.worldId,
                            levelNumber = levelDef.levelNumber,
                            title = levelDef.title,
                            missionDescription = levelDef.description,
                            stars = starsEarned,
                            completed = true
                        )
                    )
                }
            } else {
                gameDao.updateLevelProgress(levelId, starsEarned, true)
            }
            
            // Calculate reward coins
            val bonusCoins = when (starsEarned) {
                3 -> 50
                2 -> 30
                else -> 15
            }
            
            val profile = gameDao.getUserProfileDirect() ?: UserProfile()
            val newCoinBalance = profile.coins + bonusCoins
            gameDao.updateCoins(newCoinBalance)
            
            // Update achievements
            incrementAchievementProgress("first_build", 1)
            incrementAchievementProgress("coins_hoarder", bonusCoins)
            
            // Re-calculate total stars for Superstar achievement
            val levels = gameDao.getAllLevelProgress().firstOrNull() ?: emptyList()
            val totalStars = levels.sumOf { it.stars }
            updateAchievementValue("star_collector", totalStars)
            
            // If World 1 completed
            if (levelId <= 3) {
                val w1Levels = levels.filter { it.worldId == 1 }
                val completedW1Count = w1Levels.count { it.completed || it.levelId == levelId }
                updateAchievementValue("world_conqueror", completedW1Count)
            }
            
            return bonusCoins
        }
        return 0
    }

    suspend fun incrementFailures() {
        incrementAchievementProgress("failure_guru", 1)
    }

    suspend fun awardCoins(amount: Int) {
        val profile = gameDao.getUserProfileDirect() ?: UserProfile()
        val newCoinBalance = profile.coins + amount
        gameDao.updateCoins(newCoinBalance)
        incrementAchievementProgress("coins_hoarder", amount)
    }

    suspend fun purchaseCosmetic(itemId: String, cost: Int): Boolean {
        val profile = gameDao.getUserProfileDirect() ?: return false
        if (profile.coins >= cost) {
            val list = profile.unlockedCosmetics.split(",").toMutableList()
            if (!list.contains(itemId)) {
                list.add(itemId)
                val updatedUnlocked = list.joinToString(",")
                
                // Deduct coins and update unlocked
                val newCoins = profile.coins - cost
                gameDao.insertUserProfile(profile.copy(coins = newCoins, unlockedCosmetics = updatedUnlocked))
                
                // Update cosmetic achievement
                val cosmeticCount = list.count { it.startsWith("paint_") || it.startsWith("eyes_") || it.startsWith("hat_") }
                updateAchievementValue("cosmetic_king", cosmeticCount)
                
                return true
            }
        }
        return false
    }

    suspend fun updateCustomization(paint: String, eyes: String, hat: String) {
        val profile = gameDao.getUserProfileDirect() ?: return
        gameDao.updateCustomization(paint, eyes, hat, profile.unlockedCosmetics)
    }

    private suspend fun incrementAchievementProgress(id: String, increment: Int) {
        val achievements = gameDao.getAllAchievements().firstOrNull() ?: return
        val ach = achievements.find { it.id == id } ?: return
        if (!ach.unlocked) {
            val newProgress = ach.currentProgress + increment
            val isUnlocked = newProgress >= ach.targetProgress
            gameDao.updateAchievementProgress(id, newProgress.coerceAtMost(ach.targetProgress), isUnlocked)
            
            if (isUnlocked) {
                awardCoins(ach.rewardCoins)
            }
        }
    }

    private suspend fun updateAchievementValue(id: String, value: Int) {
        val achievements = gameDao.getAllAchievements().firstOrNull() ?: return
        val ach = achievements.find { it.id == id } ?: return
        if (!ach.unlocked) {
            val isUnlocked = value >= ach.targetProgress
            gameDao.updateAchievementProgress(id, value.coerceAtMost(ach.targetProgress), isUnlocked)
            
            if (isUnlocked) {
                awardCoins(ach.rewardCoins)
            }
        }
    }

    // Custom Level Repository APIs
    val allCustomLevels: Flow<List<CustomLevel>> = gameDao.getAllCustomLevels()

    suspend fun getCustomLevelById(id: Int): CustomLevel? {
        return gameDao.getCustomLevelById(id)
    }

    suspend fun createCustomLevel(customLevel: CustomLevel) {
        gameDao.insertCustomLevel(customLevel)
    }

    suspend fun deleteCustomLevel(id: Int) {
        gameDao.deleteCustomLevelById(id)
    }

    suspend fun completeCustomLevel(id: Int, stars: Int) {
        val existing = gameDao.getCustomLevelById(id)
        if (existing != null && stars > existing.starsEarned) {
            gameDao.updateCustomLevelProgress(id, stars, true)
            awardCoins(50) // Reward coins for clearing custom levels!
        }
    }
}
