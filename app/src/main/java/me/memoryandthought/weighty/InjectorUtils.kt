package me.memoryandthought.weighty

import android.content.Context
import me.memoryandthought.weighty.database.ExerciseRepositoryImpl
import me.memoryandthought.weighty.database.WeightyDatabase
import me.memoryandthought.weighty.database.WorkoutRepositoryImpl
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.fragments.EditSetDialog
import me.memoryandthought.weighty.viewmodels.*
import java.util.*

object InjectorUtils {
    fun provideExercisesViewModelFactory(context: Context): ExercisesViewModelFactory {
        val repository = ExerciseRepositoryImpl(WeightyDatabase.getInstance(context))
        return ExercisesViewModelFactory(repository)
    }

    fun provideEditExerciseViewModelFactory(context: Context, mode: FormDialogMode<Exercise>): EditExerciseViewModelFactory {
        val repository = ExerciseRepositoryImpl(WeightyDatabase.getInstance(context))
        return EditExerciseViewModelFactory(repository, mode)
    }

    fun provideExerciseHistoryViewModelFactory(context: Context, exercise: Exercise): ExerciseHistoryViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        return ExerciseHistoryViewModelFactory(workoutRepo, exercise)
    }

    fun provideEditSetViewModelFactory(context: Context, exercise: Exercise, mode: FormDialogMode<Set>): EditSetViewModelFactory {
        val db = WeightyDatabase.getInstance(context)
        val workoutRepo = WorkoutRepositoryImpl(db)
        return EditSetViewModelFactory(workoutRepo, exercise, mode)
    }

}