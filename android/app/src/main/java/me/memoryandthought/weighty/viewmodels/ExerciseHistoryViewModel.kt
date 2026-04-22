package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Workout
import java.text.DecimalFormat
import java.util.UUID

class ExerciseHistoryViewModel(
    exerciseRepo: ExerciseRepository,
    private val workoutRepo: WorkoutRepository,
    private val exerciseId: UUID
) : ViewModel() {

    val exercise: StateFlow<Exercise?> = exerciseRepo.loadExercise(viewModelScope, exerciseId)

    private val workouts: StateFlow<List<Workout>> =
        workoutRepo.loadWorkoutsForExerciseId(viewModelScope, exerciseId)

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

    fun archiveSet(exercise: Exercise, set: Set) {
        viewModelScope.launch {
            workoutRepo.archiveSetForExercise(exercise, set)
        }
    }

    fun unarchiveSet(exercise: Exercise, set: Set) {
        viewModelScope.launch {
            workoutRepo.unarchiveSetForExercise(exercise, set)
        }
    }
}

sealed class ExerciseHistoryItem
data class WorkoutHeader(val date: String): ExerciseHistoryItem()
data class SetRow(val set: Set, val weight: String, val reps: String, val rpe: String) : ExerciseHistoryItem()