package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    fun observeGoal(id: Long): Flow<Goal?>
    suspend fun getGoal(id: Long): Goal?

    /** Saves the goal's own fields (not its milestones). Inserts when [Goal.id] is 0. */
    suspend fun saveGoal(goal: Goal): Long
    suspend fun deleteGoal(id: Long)
    suspend fun setGoalCompleted(id: Long, completed: Boolean, at: Instant?)
    suspend fun addMilestone(goalId: Long, title: String)
    suspend fun setMilestoneDone(id: Long, done: Boolean)
    suspend fun deleteMilestone(id: Long)
}
