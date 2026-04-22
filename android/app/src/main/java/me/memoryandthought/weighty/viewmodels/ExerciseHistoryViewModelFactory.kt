package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import me.memoryandthought.weighty.database.ExerciseRepository
import me.memoryandthought.weighty.database.WorkoutRepository
import java.util.UUID
import kotlin.reflect.KClass

class ExerciseHistoryViewModelFactory(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseId: UUID
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ExerciseHistoryViewModel(exerciseRepository, workoutRepository, exerciseId) as T
    }
}