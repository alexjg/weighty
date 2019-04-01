package me.memoryandthought.weighty.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.database.WorkoutRepository
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Exercise
import java.lang.IllegalStateException
import java.time.Instant
import java.util.*

class EditSetViewModel(
    private val workoutRepo: WorkoutRepository,
    private val exercise: Exercise,
    private val editingSet: Set?,
    private val templateSet: Set?) : ViewModel() {
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
            return validLiveData as LiveData<Boolean>
        }

    init {
        templateSet?.let {
            rpe = templateSet.rpe.toString()
            reps = templateSet.reps.toString()
            weight = templateSet.weight.toString()
        }
        editingSet?.let {
            rpe = editingSet.rpe.toString()
            reps = editingSet.reps.toString()
            weight = editingSet.weight.toString()
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

    fun saveSet() {
        if (!checkValid()) {
            throw IllegalStateException("Invalid set data")
        }
        if (editingSet != null) {
            val updatedSet = editingSet.copy(
                weight = weight.toDouble(),
                reps = reps.toDouble(),
                rpe = rpe.toDouble()
            )
            viewModelScope.launch {
                workoutRepo.updateSetForExercise(
                    exercise, updatedSet
                )
            }
        } else {
            val set = Set(
                UUID.randomUUID(),
                weight.toDouble(),
                reps.toDouble(),
                rpe.toDouble(),
                Instant.now()
            )
            viewModelScope.launch {
                workoutRepo.addSetForExercise(exercise, set)
            }
        }
    }

}