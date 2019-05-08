package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.domain.Exercise

class EditExerciseViewModelFactory(private val repository: ExerciseRepository, private val mode: FormDialogMode<Exercise>) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditExerciseViewModel(repository, mode) as T
    }
}
