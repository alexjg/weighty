package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.database.ExerciseRepository

class ExercisesViewModel(private val repo: ExerciseRepository) : ViewModel() {
    val exercises: StateFlow<List<Exercise>> = repo.loadExercises(viewModelScope)

    fun archiveExercise(exercise: Exercise) {
        viewModelScope.launch {
            repo.archiveExercise(exercise)
        }
    }

    fun unarchiveExercise(exercise: Exercise) {
        viewModelScope.launch {
            repo.unarchiveExercise(exercise)
        }
    }
}
