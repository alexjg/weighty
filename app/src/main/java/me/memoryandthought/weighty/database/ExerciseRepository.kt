package me.memoryandthought.weighty.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.domain.Exercise
import java.time.Instant
import java.util.*

interface ExerciseRepository {
    fun loadExercises(): LiveData<List<Exercise>>
    suspend fun addExercise(name: String)
    suspend fun archiveExercise(ex: Exercise)
}

class ExerciseRepositoryImpl(private val db: WeightyDatabase) : ExerciseRepository {
    override fun loadExercises(): LiveData<List<Exercise>> {
        return Transformations.map(db.exerciseDao().loadExercises()) {
            dtos -> dtos.map { Exercise(it.id, it.name, it.created) }
        }
    }

    override suspend fun addExercise(name: String) {
        withContext(IO){
            val id = UUID.randomUUID()
            val created = Instant.now()
            db.exerciseDao().insertExercise(ExerciseDTO(id, name, created, false))
        }
    }

    override suspend fun archiveExercise(ex: Exercise) {
        withContext(IO) {
            val dto = ExerciseDTO(ex).copy(archived = true)
            db.exerciseDao().updateExercise(dto)
        }
    }

}