package me.memoryandthought.weighty

import android.content.Context
import me.memoryandthought.weighty.database.ExerciseRepositoryImpl
import me.memoryandthought.weighty.database.WeightyDatabase
import me.memoryandthought.weighty.database.WorkoutRepositoryImpl
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.EditSetViewModelFactory
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryViewModelFactory
import me.memoryandthought.weighty.viewmodels.ExercisesViewModelFactory
import java.util.*

object InjectorUtils {
    fun provideExercisesViewModelFactory(context: Context): ExercisesViewModelFactory {
        val repository = ExerciseRepositoryImpl(WeightyDatabase.getInstance(context))
        return ExercisesViewModelFactory(repository)
    }

    fun provideExerciseHistoryViewModelFactory(context: Context, exercise: Exercise): ExerciseHistoryViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        return ExerciseHistoryViewModelFactory(workoutRepo, exercise)
    }

    fun provideEditSetViewModelFactory(context: Context, exercise: Exercise, editingSet: Set?): EditSetViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        return EditSetViewModelFactory(workoutRepo, exercise, editingSet)
    }

}