package me.memoryandthought.weighty.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Workout
import me.memoryandthought.weighty.domain.Set
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

interface WorkoutRepository {
    fun loadWorkoutsForExerciseId(exerciseId: UUID): LiveData<List<Workout>>
}

class WorkoutRepositoryImpl(private val db: WeightyDatabase) : WorkoutRepository {
    override fun loadWorkoutsForExerciseId(exerciseId: UUID): LiveData<List<Workout>> {
        val localTimeZone = ZonedDateTime.now().zone
        val setDtos = db.workoutDao().exerciseSetsForExercise(exerciseId)
        return Transformations.map(setDtos) { setDtos ->
            setDtos.groupBy({ LocalDateTime.ofInstant(it.timestamp, localTimeZone).toLocalDate()}).entries.map{ (date, setDtos) ->
                val sets = setDtos.map {
                    Set(it.weight, it.reps, it.rpe, it.timestamp)
                }
                val aSetDto = setDtos.first()
                val exercise = Exercise(aSetDto.exerciseId, aSetDto.exerciseName, aSetDto.exerciseCreated)
                Workout(date, exercise, sets)
            }
        }
    }

}


