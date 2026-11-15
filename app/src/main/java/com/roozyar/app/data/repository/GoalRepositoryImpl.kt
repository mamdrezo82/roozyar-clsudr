package com.roozyar.app.data.repository

import com.roozyar.app.core.database.GoalDao
import com.roozyar.app.core.database.GoalEntity
import com.roozyar.app.core.database.MilestoneEntity
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.Milestone
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

fun MilestoneEntity.toDomain() = Milestone(id, goalId, title, done, position)

fun GoalEntity.toDomain(milestones: List<MilestoneEntity> = emptyList()) = Goal(
    id = id,
    title = title,
    description = description,
    category = GoalCategory.fromName(category),
    priority = TaskPriority.fromLevel(priority),
    deadline = deadlineEpochDay?.let(LocalDate::ofEpochDay),
    completed = completed,
    completedAt = completedAtEpochMillis?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    milestones = milestones.map { it.toDomain() },
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    priority = priority.level,
    deadlineEpochDay = deadline?.toEpochDay(),
    completed = completed,
    completedAtEpochMillis = completedAt?.toEpochMilli(),
    createdAtEpochMillis = createdAt.toEpochMilli(),
)

class GoalRepositoryImpl @Inject constructor(private val dao: GoalDao) : GoalRepository {
    override fun observeGoals(): Flow<List<Goal>> = combine(dao.observeGoals(), dao.observeAllMilestones()) { goals, milestones ->
        val byGoal = milestones.groupBy { it.goalId }
        goals.map { it.toDomain(byGoal[it.id].orEmpty()) }
    }

    override fun observeGoal(id: Long): Flow<Goal?> = combine(dao.observeGoal(id), dao.observeMilestones(id)) { goal, milestones ->
        goal?.toDomain(milestones)
    }

    override suspend fun getGoal(id: Long): Goal? = dao.getById(id)?.toDomain()

    override suspend fun saveGoal(goal: Goal): Long =
        if (goal.id == 0L) {
            dao.insert(goal.toEntity())
        } else {
            dao.update(goal.toEntity())
            goal.id
        }

    override suspend fun deleteGoal(id: Long) = dao.deleteById(id)

    override suspend fun setGoalCompleted(id: Long, completed: Boolean, at: Instant?) =
        dao.setCompleted(id, completed, at?.toEpochMilli())

    override suspend fun addMilestone(goalId: Long, title: String) {
        dao.insertMilestone(MilestoneEntity(goalId = goalId, title = title, position = dao.nextMilestonePosition(goalId)))
    }

    override suspend fun setMilestoneDone(id: Long, done: Boolean) = dao.setMilestoneDone(id, done)

    override suspend fun deleteMilestone(id: Long) = dao.deleteMilestone(id)
}
