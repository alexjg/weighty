package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.memoryandthought.weighty.database.ExerciseRepository

class ExercisesViewModelFactory(private val repository: ExerciseRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ExercisesViewModel(repository) as T
    }
}