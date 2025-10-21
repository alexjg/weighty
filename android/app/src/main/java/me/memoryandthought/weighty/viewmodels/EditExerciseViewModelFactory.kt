package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.domain.Exercise
import kotlin.reflect.KClass

class EditExerciseViewModelFactory(private val repository: ExerciseRepository, private val mode: FormDialogMode<Exercise>) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return EditExerciseViewModel(repository, mode) as T
    }
}
