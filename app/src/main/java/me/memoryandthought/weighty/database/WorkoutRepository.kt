package me.memoryandthought.weighty.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Workout
import me.memoryandthought.weighty.domain.Set
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

interface WorkoutRepository {
    fun loadWorkoutsForExerciseId(exerciseId: UUID): LiveData<List<Workout>>
    suspend fun addSetForExercise(exercise: Exercise, set: Set)
}

class WorkoutRepositoryImpl(private val db: WeightyDatabase) : WorkoutRepository {
    override fun loadWorkoutsForExerciseId(exerciseId: UUID): LiveData<List<Workout>> {
        val localTimeZone = ZonedDateTime.now().zone
        val setDtos = db.workoutDao().exerciseSetsForExercise(exerciseId)
        return Transformations.map(setDtos) { setDtos ->
            setDtos.groupBy({ LocalDateTime.ofInstant(it.timestamp, localTimeZone).toLocalDate()}).entries.map{ (date, setDtos) ->
                val sets = setDtos.map {
                    Set(it.setId, it.weight, it.reps, it.rpe, it.timestamp)
                }
                val aSetDto = setDtos.first()
                val exercise = Exercise(aSetDto.exerciseId, aSetDto.exerciseName, aSetDto.exerciseCreated)
                Workout(date, exercise, sets)
            }
        }
    }

    override suspend fun addSetForExercise(exercise: Exercise, set: Set) {
        val dto = SetDTO(set.id, set.timestamp, set.weight, set.reps, set.rpe, false, exercise.id)
        withContext(IO) {
            db.setDao().insert(dto)
        }
    }

}


