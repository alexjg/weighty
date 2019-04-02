package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.database.ExerciseRepository
import java.time.Instant
import java.util.UUID

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


    fun save(name: String){
        when (mode){
            is Create, is CreateFromTemplate -> {
                viewModelScope.launch {
                    repo.addExercise(name)
                }
            }
            is Edit -> {
                val exercise = mode.data.copy(name=name)
                viewModelScope.launch {
                    repo.updateExercise(exercise)
                }
            }
        }
    }

}
