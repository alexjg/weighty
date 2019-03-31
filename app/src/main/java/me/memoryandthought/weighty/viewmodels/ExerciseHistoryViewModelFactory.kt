package me.memoryandthought.weighty.viewmodels;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository;
import me.memoryandthought.weighty.domain.Exercise
import java.util.*

class ExerciseHistoryViewModelFactory(
    private val repository: WorkoutRepository,
    private val exercise: Exercise) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ExerciseHistoryViewModel(repository, exercise) as T
    }
}
