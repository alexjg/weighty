package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.memoryandthought.weighty.database.WorkoutRepository;
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set

class EditSetViewModelFactory(
    private val repository: WorkoutRepository,
    private val exercise: Exercise,
    private val editingSet: Set?) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditSetViewModel(repository, exercise, editingSet) as T
    }
}
