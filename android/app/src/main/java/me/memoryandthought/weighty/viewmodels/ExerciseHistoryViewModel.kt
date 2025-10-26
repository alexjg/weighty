package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Workout
import java.text.DecimalFormat

class ExerciseHistoryViewModel(private val repo: WorkoutRepository, private val exercise: Exercise): ViewModel() {

    private val workouts: StateFlow<List<Workout>> =
        repo.loadWorkoutsForExerciseId(viewModelScope, exercise.id)

    val historyItems: StateFlow<List<ExerciseHistoryItem>> =
        workouts.map { workoutList ->
            val df = DecimalFormat("#.#")
            workoutList.flatMap { workout ->
                val header = WorkoutHeader(workout.date.toString()) as ExerciseHistoryItem
                val setRows: List<ExerciseHistoryItem> = workout.sets.map {
                    SetRow(
                        it,
                        it.weight.toString(),
                        df.format(it.reps),
                        it.rpe.toString()
                    )
                }
                listOf(header) + setRows
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val mostRecentSet: StateFlow<Set?> =
        workouts.map { workoutList ->
            workoutList.firstOrNull()?.sets?.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
