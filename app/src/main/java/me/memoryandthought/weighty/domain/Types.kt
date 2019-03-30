package me.memoryandthought.weighty.domain
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Exercise(val id: UUID, val name: String, val created: Instant)
data class Set(val weight: Double, val reps: Double, val rpe: Double, val timestamp: Instant)
data class Workout(val data: LocalDate, val exercise: Exercise, val sets: List<Set>)
