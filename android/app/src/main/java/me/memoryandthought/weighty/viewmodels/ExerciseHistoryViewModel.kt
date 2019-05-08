package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Workout
import java.text.DecimalFormat
import java.util.*

class ExerciseHistoryViewModel(private val repo: WorkoutRepository,  private val exercise: Exercise): ViewModel() {

    fun historyItems(): LiveData<List<ExerciseHistoryItem>> {
        return Transformations.map(repo.loadWorkoutsForExerciseId(exercise.id)) {workouts ->
            val df= DecimalFormat("#.#")
            workouts.map { workout: Workout ->
                val header = WorkoutHeader(workout.date.toString()) as ExerciseHistoryItem
                val setRows: List<ExerciseHistoryItem> = workout.sets.map {
                    SetRow(
                        it,
                        it.weight.toString(),
                        df.format(it.reps),
                        it.rpe.toString()
                    ) as ExerciseHistoryItem
                }
                listOf(header) + setRows

            }.flatten()
        }
    }

    fun mostRecentSet(): LiveData<Set?> {
        return Transformations.map(repo.loadWorkoutsForExerciseId(exercise.id)) { workouts ->
            workouts.firstOrNull()?.let {
                it.sets.firstOrNull()
            }
        }
    }

    fun archiveSet(set: Set) {
        viewModelScope.launch {
            repo.archiveSetForExercise(exercise, set)
        }
    }

    fun unarchiveSet(set: Set) {
        viewModelScope.launch {
            repo.unarchiveSetForExercise(exercise, set)
        }
    }

}

sealed class ExerciseHistoryItem
data class WorkoutHeader(val date: String): ExerciseHistoryItem()
data class SetRow(val set: Set, val weight: String, val reps: String, val rpe: String) : ExerciseHistoryItem()