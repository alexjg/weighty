package me.memoryandthought.weighty

import android.content.Context
import me.memoryandthought.weighty.database.ExerciseRepositoryImpl
import me.memoryandthought.weighty.database.WeightyDatabase
import me.memoryandthought.weighty.database.WorkoutRepositoryImpl
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.EditSetViewModelFactory
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryViewModelFactory
import me.memoryandthought.weighty.viewmodels.ExercisesViewModelFactory
import java.util.*

object InjectorUtils {
    fun provideExercisesViewModelFactory(context: Context): ExercisesViewModelFactory {
        val repository = ExerciseRepositoryImpl(WeightyDatabase.getInstance(context))
        return ExercisesViewModelFactory(repository)
    }

    fun provideExerciseHistoryViewModelFactory(context: Context, exerciseId: UUID): ExerciseHistoryViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        val exerciseRepo = ExerciseRepositoryImpl(db)
        return ExerciseHistoryViewModelFactory(workoutRepo, exerciseRepo, exerciseId)
    }

    fun provideEditSetViewModelFactory(context: Context, exercise: Exercise): EditSetViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        return EditSetViewModelFactory(workoutRepo, exercise)
    }

}