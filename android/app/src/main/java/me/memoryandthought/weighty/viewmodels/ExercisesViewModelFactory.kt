package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import me.memoryandthought.weighty.database.ExerciseRepository
import kotlin.reflect.KClass

class ExercisesViewModelFactory(private val repository: ExerciseRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ExercisesViewModel(repository) as T
    }
}