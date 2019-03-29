package me.memoryandthought.weighty.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.memoryandthought.weighty.Exercise
import java.time.Instant
import java.util.*

@Entity(tableName = "exercise")
data class ExerciseDTO(
    @PrimaryKey val id: UUID,
    val name: String,
    val created: Instant,
    val archived: Boolean
) {
    constructor(ex: Exercise) : this(ex.id, ex.name,  ex.created, false)
}

