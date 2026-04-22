package me.memoryandthought.weighty

import android.content.Context
import java.util.UUID
import me.memoryandthought.weighty.automerge.WeightyRepo
import me.memoryandthought.weighty.database.ExerciseRepositoryImpl
import me.memoryandthought.weighty.database.WorkoutRepositoryImpl
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.*
import org.automerge.repo.DocHandle

object InjectorUtils {

    private var docHandle: DocHandle? = null

    suspend fun init(context: Context) {
        val repo = WeightyRepo.getInstance(context)
        docHandle = repo.getDocHandle()
    }

    private fun requireDocHandle(): DocHandle {
        return docHandle ?: throw IllegalStateException(
            "InjectorUtils.init() must be called before accessing repositories"
        )
    }

    fun provideExercisesViewModelFactory(context: Context): ExercisesViewModelFactory {
        val repository = ExerciseRepositoryImpl(requireDocHandle())
        return ExercisesViewModelFactory(repository)
    }

    fun provideEditExerciseViewModelFactory(context: Context, mode: FormDialogMode<Exercise>): EditExerciseViewModelFactory {
        val repository = ExerciseRepositoryImpl(requireDocHandle())
        return EditExerciseViewModelFactory(repository, mode)
    }

    fun provideExerciseHistoryViewModelFactory(context: Context, exerciseId: UUID): ExerciseHistoryViewModelFactory {
        val exerciseRepo = ExerciseRepositoryImpl(requireDocHandle())
        val workoutRepo = WorkoutRepositoryImpl(requireDocHandle())
        return ExerciseHistoryViewModelFactory(exerciseRepo, workoutRepo, exerciseId)
    }

    fun provideEditSetViewModelFactory(context: Context, exercise: Exercise, mode: FormDialogMode<Set>): EditSetViewModelFactory {
        val workoutRepo = WorkoutRepositoryImpl(requireDocHandle())
        return EditSetViewModelFactory(workoutRepo, exercise, mode)
    }
}
