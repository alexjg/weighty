package me.memoryandthought.weighty.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.domain.Exercise
import java.time.Instant
import java.util.*

interface ExerciseRepository {
    fun loadExercises(): LiveData<List<Exercise>>
    fun loadExercise(id: UUID): LiveData<Exercise?>
    suspend fun addExercise(name: String)
    suspend fun updateExercise(ex: Exercise)
    suspend fun archiveExercise(ex: Exercise)
    suspend fun unarchiveExercise(ex: Exercise)
}

class ExerciseRepositoryImpl(private val db: WeightyDatabase) : ExerciseRepository {
    override fun loadExercises(): LiveData<List<Exercise>> {
        return Transformations.map(db.exerciseDao().loadExercises()) {
            dtos -> dtos.map {  Exercise(it.id, it.name, it.created) }
        }
    }

    override fun loadExercise(id: UUID): LiveData<Exercise?> {
        return Transformations.map(db.exerciseDao().loadExercise(id)){ dto ->
            dto?.let {
                Exercise(dto.id, dto.name, dto.created)
            }
        }
    }

    override suspend fun addExercise(name: String) {
        withContext(IO){
            val id = UUID.randomUUID()
            val created = Instant.now()
            db.exerciseDao().insertExercise(ExerciseDTO(id, name, created, false))
        }
    }

    override suspend fun updateExercise(ex: Exercise) {
        withContext(IO) {
            val dto = ExerciseDTO(ex)
            db.exerciseDao().updateExercise(dto)
        }
    }

    override suspend fun archiveExercise(ex: Exercise) {
        withContext(IO) {
            val dto = ExerciseDTO(ex).copy(archived = true)
            db.exerciseDao().updateExercise(dto)
        }
    }

    override suspend fun unarchiveExercise(ex: Exercise) {
        withContext(IO) {
            val dto = ExerciseDTO(ex).copy(archived = false)
            db.exerciseDao().updateExercise(dto)
        }
    }

}