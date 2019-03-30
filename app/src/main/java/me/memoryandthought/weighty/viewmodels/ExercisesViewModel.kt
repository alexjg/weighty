package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.database.ExerciseRepository

class ExercisesViewModel(private val repo: ExerciseRepository) : ViewModel() {
    fun loadExercises(): LiveData<List<Exercise>> {
        return repo.loadExercises()
    }

    fun addExercise(name: String) {
        viewModelScope.launch {
            repo.addExercise(name)
        }
    }
}