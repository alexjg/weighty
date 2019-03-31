package me.memoryandthought.weighty.database

import androidx.room.*
import me.memoryandthought.weighty.domain.Exercise
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

@DatabaseView("SELECT exercise.*, SetDTO.timestamp as lastSetTimestamp " +
        "FROM exercise left outer join SetDTO on SetDTO.id = (" +
        "   SELECT id from SetDTO " +
        "   where SetDTO.exerciseId = exercise.id " +
        "   order by timestamp desc" +
        "   limit 1" +
        ") order by lastSetTimestamp desc")
data class ExerciseWithTimestampDTO(
    val id: UUID,
    val name: String,
    val created: Instant,
    val archived: Boolean,
    val lastSetTimestamp: Instant?
)

@Entity(
    tableName = "SetDTO",
    foreignKeys = arrayOf(ForeignKey(
        entity = ExerciseDTO::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("exerciseId"))
    ),
    indices = arrayOf(
        Index(value=["exerciseId"])
    )
)
data class SetDTO(
    @PrimaryKey val id: UUID,
    val timestamp: Instant,
    val weight: Double,
    val reps: Double,
    val rpe: Double,
    val archived: Boolean,
    val exerciseId: UUID
)


@DatabaseView("SELECT SetDTO.id as setId, SetDTO.timestamp, SetDTO.weight, SetDTO.reps, " +
        "SetDTO.rpe, exercise.id as exerciseId, exercise.name as exerciseName, exercise.created as exerciseCreated " +
        "FROM exercise INNER JOIN SetDTO on SetDTO.exerciseId = exercise.id where not exercise.archived and not SetDTO.archived"

)
data class ExerciseSetDTO(
    val exerciseId: UUID,
    val exerciseName: String,
    val exerciseCreated: Instant,
    val setId: UUID,
    val timestamp: Instant,
    val weight: Double,
    val rpe: Double,
    val reps: Double
)

