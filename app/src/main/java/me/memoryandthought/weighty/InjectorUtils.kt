package me.memoryandthought.weighty

import android.content.Context
import me.memoryandthought.weighty.database.ExerciseRepositoryImpl
import me.memoryandthought.weighty.database.WeightyDatabase
import me.memoryandthought.weighty.viewmodels.ExercisesViewModelFactory

object InjectorUtils {
    fun provideExercisesViewModelFactory(context: Context): ExercisesViewModelFactory {
        val repository = ExerciseRepositoryImpl(WeightyDatabase.getInstance(context))
        return ExercisesViewModelFactory(repository)
    }
}