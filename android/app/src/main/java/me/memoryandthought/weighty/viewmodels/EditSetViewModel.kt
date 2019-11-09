package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Exercise
import java.text.DecimalFormat
import java.time.Instant
import java.util.UUID

class EditSetViewModel(
    private val workoutRepo: WorkoutRepository,
    private val exercise: Exercise,
    private val mode: FormDialogMode<Set>): ViewModel() {

    var rpe: String = ""
        set(value) {
            field = value
            updateValidity()
        }
    var weight: String = ""
        set(value) {
            field = value
            updateValidity()
        }
    var reps: String = ""
        set(value) {
            field = value
            updateValidity()
        }
    private val validLiveData = MutableLiveData<Boolean>()
    val isValid: LiveData<Boolean>
        get() {
            return validLiveData
        }

    val title: String
        get() {
            return when (mode) {
                is Create, is CreateFromTemplate -> "Add set"
                is Edit<Set> -> "Edit set"
            }
        }

    val positiveButton: String
        get() {
            return when (mode) {
                is Create, is CreateFromTemplate -> "Add"
                is Edit<Set> -> "Update"
            }
        }

    init {
        when (mode) {
            is Edit<Set> -> {
                rpe = mode.data.rpe.toString()
                val df = DecimalFormat("#")
                reps = df.format(mode.data.reps)
                weight = mode.data.weight.toString()
            }
            is CreateFromTemplate -> {
               rpe = mode.template.rpe.toString()
               val df = DecimalFormat("#")
               reps = df.format(mode.template.reps)
               weight = mode.template.weight.toString()
            }
        }
        validLiveData.postValue(checkValid())
    }

    private fun updateValidity() {
        validLiveData.value = checkValid()
    }

    private fun checkValid(): Boolean {
        return validateRpe() and validateReps() and validateWeight()
    }

    private fun validateRpe(): Boolean {
        rpe.toDoubleOrNull()?.let {
            if ((it >= 0) and (it <= 10.0)){
                return true
            }
        }
        return false
    }

    private fun validateWeight(): Boolean {
        weight.toDoubleOrNull()?.let {
            if (it >= 0){
                return true
            }
        }
        return false
    }

    private fun validateReps(): Boolean {
        reps.toDoubleOrNull()?.let {
            if (it > 0.0) {
                return true
            }
        }
        return false
    }

    suspend fun saveSet() {
        check(checkValid()) { "Invalid set data" }
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            when (mode) {
                is Create, is CreateFromTemplate -> {
                    val set = Set(
                        UUID.randomUUID(),
                        weight.toDouble(),
                        reps.toDouble(),
                        rpe.toDouble(),
                        Instant.now()
                    )
                    workoutRepo.addSetForExercise(exercise, set)
                }
                is Edit<Set> -> {
                    val updatedSet = mode.data.copy(
                        weight = weight.toDouble(),
                        reps = reps.toDouble(),
                        rpe = rpe.toDouble()
                    )
                    workoutRepo.updateSetForExercise(
                        exercise, updatedSet
                    )
                }
            }
        }
    }

}