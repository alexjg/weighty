package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Workout
import java.text.DecimalFormat
import java.util.*

class ExerciseHistoryViewModel(private val repo: WorkoutRepository,  private val exerciseRepo: ExerciseRepository, private val exerciseId: UUID): ViewModel() {

    fun exercise(): LiveData<Exercise?> {
        return exerciseRepo.loadExercise(exerciseId)
    }

    fun historyItems(): LiveData<List<ExerciseHistoryItem>> {
        return Transformations.map(repo.loadWorkoutsForExerciseId(exerciseId)) {workouts ->
            val df= DecimalFormat("#.#")
            workouts.map { workout: Workout ->
                val header = WorkoutHeader(workout.date.toString()) as ExerciseHistoryItem
                val setRows: List<ExerciseHistoryItem> = workout.sets.map {
                    SetRow(
                        it.weight.toString(),
                        df.format(it.reps),
                        it.rpe.toString()
                    ) as ExerciseHistoryItem
                }
                listOf(header) + setRows

            }.flatten()
        }
    }

}

sealed class ExerciseHistoryItem
data class WorkoutHeader(val date: String): ExerciseHistoryItem()
data class SetRow(val weight: String, val reps: String, val rpe: String) : ExerciseHistoryItem()