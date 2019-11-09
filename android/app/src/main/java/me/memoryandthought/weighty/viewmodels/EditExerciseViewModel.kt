package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.database.ExerciseRepository

class EditExerciseViewModel(
    private val repo: ExerciseRepository,
    private val mode: FormDialogMode<Exercise>
    ) : ViewModel() {

    var name: String = ""

    val titleResource: Int
        get() {
            return when (mode) {
                is Create, is CreateFromTemplate -> R.string.create_exercise_title
                is Edit -> R.string.edit_exercise_title
            }
        }

    val positiveButtonResource: Int
        get() {
            return when (mode) {
                is Create, is CreateFromTemplate -> R.string.create_exercise_positive
                is Edit -> R.string.edit_exercise_positive
            }
        }

    init {
        name = when(mode) {
            is Create, is CreateFromTemplate -> ""
            is Edit -> mode.data.name
        }
    }


    suspend fun save(name: String){
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            when (mode){
                is Create, is CreateFromTemplate -> {
                    repo.addExercise(name)
                }
                is Edit -> {
                    val exercise = mode.data.copy(name=name)
                    repo.updateExercise(exercise)
                }
            }

        }
    }

}
