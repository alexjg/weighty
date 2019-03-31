package me.memoryandthought.weighty.viewmodels;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository;
import java.util.*

class ExerciseHistoryViewModelFactory(
    private val repository: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    private val exerciseId: UUID) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ExerciseHistoryViewModel(repository, exerciseRepo, exerciseId) as T
    }
}
