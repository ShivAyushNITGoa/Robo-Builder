package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Level Progress
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevelProgress(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevelProgressById(levelId: Int): LevelProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelProgress(progress: LevelProgress)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllLevels(levels: List<LevelProgress>)

    @Query("UPDATE level_progress SET stars = :stars, completed = :completed WHERE levelId = :levelId")
    suspend fun updateLevelProgress(levelId: Int, stars: Int, completed: Boolean)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET coins = :coins WHERE id = 1")
    suspend fun updateCoins(coins: Int)

    @Query("UPDATE user_profile SET selectedPaint = :paint, selectedEyes = :eyes, selectedHat = :hat, unlockedCosmetics = :unlocked WHERE id = 1")
    suspend fun updateCustomization(paint: String, eyes: String, hat: String, unlocked: String)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAchievements(achievements: List<Achievement>)

    @Query("UPDATE achievements SET currentProgress = :progress, unlocked = :unlocked WHERE id = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int, unlocked: Boolean)

    // Custom Levels
    @Query("SELECT * FROM custom_levels ORDER BY dateCreated DESC")
    fun getAllCustomLevels(): Flow<List<CustomLevel>>

    @Query("SELECT * FROM custom_levels WHERE id = :id LIMIT 1")
    suspend fun getCustomLevelById(id: Int): CustomLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLevel(customLevel: CustomLevel)

    @Query("DELETE FROM custom_levels WHERE id = :id")
    suspend fun deleteCustomLevelById(id: Int)

    @Query("UPDATE custom_levels SET starsEarned = :stars, completed = :completed WHERE id = :id")
    suspend fun updateCustomLevelProgress(id: Int, stars: Int, completed: Boolean)
}
