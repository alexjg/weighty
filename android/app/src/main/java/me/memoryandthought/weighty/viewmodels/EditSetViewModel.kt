package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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

    private val _rpe = MutableStateFlow("")
    val rpe: StateFlow<String> = _rpe

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight

    private val _reps = MutableStateFlow("")
    val reps: StateFlow<String> = _reps

    val isValid: StateFlow<Boolean> = combine(_rpe, _weight, _reps) { rpe, weight, reps ->
        validateRpe(rpe) && validateWeight(weight) && validateReps(reps)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    private fun validateRpe(rpe: String): Boolean {
        rpe.toDoubleOrNull()?.let {
            if (it in 0.0..10.0) return true
        }
        return false
    }

    private fun validateWeight(weight: String): Boolean {
        weight.toDoubleOrNull()?.let {
            if (it >= 0) return true
        }
        return false
    }

    private fun validateReps(reps: String): Boolean {
        reps.toDoubleOrNull()?.let {
            if (it > 0.0) return true
        }
        return false
    }

    suspend fun saveSet() {
        check(isValid.value) { "Invalid set data" }
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            when (mode) {
                is Create, is CreateFromTemplate -> {
                    val set = Set(
                        UUID.randomUUID(),
                        _weight.value.toDouble(),
                        _reps.value.toDouble(),
                        _rpe.value.toDouble(),
                        Instant.now()
                    )
                    workoutRepo.addSetForExercise(exercise, set)
                }
                is Edit<Set> -> {
                    val updatedSet = mode.data.copy(
                        id = mode.data.id,
                        weight = _weight.value.toDouble(),
                        reps = _reps.value.toDouble(),
                        rpe = _rpe.value.toDouble()
                    )
                    workoutRepo.updateSetForExercise(exercise, updatedSet)
                }
            }
        }
    }
}
