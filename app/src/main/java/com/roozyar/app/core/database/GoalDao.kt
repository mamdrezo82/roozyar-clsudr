package com.roozyar.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY completed ASC, createdAtEpochMillis DESC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM milestones ORDER BY position ASC, id ASC")
    fun observeAllMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoal(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY position ASC, id ASC")
    fun observeMilestones(goalId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE goals SET completed = :completed, completedAtEpochMillis = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM milestones WHERE goalId = :goalId")
    suspend fun nextMilestonePosition(goalId: Long): Int

    @Insert
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Query("UPDATE milestones SET done = :done WHERE id = :id")
    suspend fun setMilestoneDone(id: Long, done: Boolean)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)
}
