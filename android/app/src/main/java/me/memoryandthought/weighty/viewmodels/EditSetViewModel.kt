package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import java.text.DecimalFormat
import java.time.Instant
import java.util.UUID

class EditSetViewModel(
    private val workoutRepo: WorkoutRepository,
    private val exercise: Exercise,
    private val mode: FormDialogMode<Set>
) : ViewModel() {

    private val _rpe = MutableLiveData("")
    val rpe: LiveData<String> = _rpe

    private val _weight = MutableLiveData("")
    val weight: LiveData<String> = _weight

    private val _reps = MutableLiveData("")
    val reps: LiveData<String> = _reps

    private val _isValid = MediatorLiveData<Boolean>()
    val isValid: LiveData<Boolean> = _isValid

    val title:
        String
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
                _rpe.value = mode.data.rpe.toString()
                val df = DecimalFormat("#")
                _reps.value = df.format(mode.data.reps)
                _weight.value = mode.data.weight.toString()
            }
            is CreateFromTemplate -> {
                _rpe.value = mode.template.rpe.toString()
                val df = DecimalFormat("#")
                _reps.value = df.format(mode.template.reps)
                _weight.value = mode.template.weight.toString()
            }
            Create -> {
                _rpe.value = "8.0"
                val df = DecimalFormat("#")
                _reps.value = df.format(0)
                _weight.value = "0"
            }
        }

        _isValid.addSource(_rpe) { updateValidity() }
        _isValid.addSource(_weight) { updateValidity() }
        _isValid.addSource(_reps) { updateValidity() }
    }

    fun onRpeChange(rpe: String) {
        _rpe.value = rpe
    }

    fun onWeightChange(weight: String) {
        _weight.value = weight
    }

    fun onRepsChange(reps: String) {
        _reps.value = reps
    }

    private fun updateValidity() {
        _isValid.value = checkValid()
    }

    private fun checkValid(): Boolean {
        return validateRpe() && validateReps() && validateWeight()
    }

    private fun validateRpe(): Boolean {
        _rpe.value?.toDoubleOrNull()?.let {
            if ((it >= 0) && (it <= 10.0)) {
                return true
            }
        }
        return false
    }

    private fun validateWeight(): Boolean {
        _weight.value?.toDoubleOrNull()?.let {
            if (it >= 0) {
                return true
            }
        }
        return false
    }

    private fun validateReps(): Boolean {
        _reps.value?.toDoubleOrNull()?.let {
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
                        _weight.value!!.toDouble(),
                        _reps.value!!.toDouble(),
                        _rpe.value!!.toDouble(),
                        Instant.now()
                    )
                    workoutRepo.addSetForExercise(exercise, set)
                }
                is Edit<Set> -> {
                    val updatedSet = mode.data.copy(
                        id = mode.data.id,
                        weight = _weight.value!!.toDouble(),
                        reps = _reps.value!!.toDouble(),
                        rpe = _rpe.value!!.toDouble()
                    )
                    workoutRepo.updateSetForExercise(
                        exercise, updatedSet
                    )
                }
            }
        }
    }

}
