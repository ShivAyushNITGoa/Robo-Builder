package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int,
    val worldId: Int,
    val levelNumber: Int,
    val title: String,
    val missionDescription: String,
    val stars: Int, // 0 to 3
    val completed: Boolean
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 100,
    val selectedPaint: String = "#3D5AFE", // Bright M3 Blue
    val selectedEyes: String = "digital", // digital, retro, laser, glass
    val selectedHat: String = "none", // none, crown, top_hat, builder
    val unlockedCosmetics: String = "paint_blue,eyes_digital" // Comma-separated list
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val rewardCoins: Int = 50
)

@Entity(tableName = "custom_levels")
data class CustomLevel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val authorName: String = "Creator Bot",
    val difficulty: String = "Medium", // Easy, Medium, Hard, Expert, Insane
    
    // Part configurations allowed (Comma separated list)
    val allowedLegs: String = "Wheels,Tank Tracks,Spider Legs,Hover Engine,Jump Springs,Jetpack",
    val allowedArms: String = "Empty,Grabber,Magnet,Hammer,Drill,Welding Torch,Shield Arm",
    val allowedUtilities: String = "Empty,Battery Pack,Turbo Battery,Cooling System,Object Detector,Heat Sensor",
    
    // Advanced data parameters
    val maxWeight: Int = 60,         // Limits parts weight
    val maxEnergyCost: Int = 100,    // Limits energy consumption speed
    val batteryCapacity: Int = 1000,  // Simulation duration/steps
    
    // Obstacle placement layout: (Format: type:percent,type:percent) e.g. "lava:0.3,wall:0.7"
    val obstaclesLayout: String = "lava:0.4",
    
    // Multi-solution complex verification goals
    // Format: "GoalName|GoalDesc|CheckType:Params;Goal2Name|Goal2Desc|CheckType:Params"
    // CheckType options: 
    // - "LEG:part1,part2" (One of these legs is required)
    // - "ARM:part1,part2" (One of these arms is required)
    // - "UTIL:part1,part2" (One of these utilities is required)
    // - "WEIGHT:max" (Total weight must be less than or equal to max)
    // - "ENERGY:max" (Total energy cost must be less than or equal to max)
    val objectiveGoals: String = "Traverse Rift|Assemble Jetpack or Hover to fly across|LEG:Jetpack,Hover Engine",
    
    val completed: Boolean = false,
    val starsEarned: Int = 0,
    val dateCreated: Long = System.currentTimeMillis()
)
