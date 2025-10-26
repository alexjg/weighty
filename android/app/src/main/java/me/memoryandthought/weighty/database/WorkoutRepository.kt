package me.memoryandthought.weighty.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.automerge.AmValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Read
import org.automerge.kotlin.getBoolean
import org.automerge.kotlin.getDouble
import org.automerge.kotlin.getListId
import org.automerge.kotlin.getLong
import org.automerge.kotlin.getString
import org.automerge.kotlin.observe
import org.automerge.kotlin.withDocAsync
import org.automerge.repo.DocHandle
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.domain.Workout
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

interface WorkoutRepository {
    fun loadWorkoutsForExerciseId(scope: CoroutineScope, exerciseId: UUID): StateFlow<List<Workout>>
    suspend fun addSetForExercise(exercise: Exercise, set: Set)
    suspend fun updateSetForExercise(exercise: Exercise, set: Set)
    suspend fun archiveSetForExercise(exercise: Exercise, set: Set)
    suspend fun unarchiveSetForExercise(exercise: Exercise, set: Set)
}

class WorkoutRepositoryImpl(private val handle: DocHandle) : WorkoutRepository {

    override fun loadWorkoutsForExerciseId(scope: CoroutineScope, exerciseId: UUID): StateFlow<List<Workout>> {
        return handle.observe(scope, emptyList()) { doc ->
            val exercisesId = doc.getListId(ObjectId.ROOT, "exercises") ?: return@observe emptyList()
            val exerciseObjId = findExerciseObjectId(doc, exercisesId, exerciseId)
                ?: return@observe emptyList()
            val exercise = readExercise(doc, exerciseObjId) ?: return@observe emptyList()
            readWorkouts(doc, exerciseObjId, exercise)
        }
    }

    override suspend fun addSetForExercise(exercise: Exercise, set: Set) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val exObjId = findExerciseObjectId(tx, exercisesId, exercise.id) ?: return@use
                val setsId = tx.getListId(exObjId, "sets") ?: return@use
                val len = tx.length(setsId)
                val itemId = tx.insert(setsId, len, ObjectType.MAP)
                writeSet(tx, itemId, set)
                tx.commit()
            }
            null
        }
    }

    override suspend fun updateSetForExercise(exercise: Exercise, set: Set) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val exObjId = findExerciseObjectId(tx, exercisesId, exercise.id) ?: return@use
                val setsId = tx.getListId(exObjId, "sets") ?: return@use
                val index = findSetIndex(tx, setsId, set.id) ?: return@use
                val item = tx.get(setsId, index.toLong()).orElse(null) as? AmValue.Map ?: return@use
                writeSet(tx, item.id, set)
                tx.commit()
            }
            null
        }
    }

    override suspend fun archiveSetForExercise(exercise: Exercise, set: Set) {
        setSetArchived(exercise.id, set.id, true)
    }

    override suspend fun unarchiveSetForExercise(exercise: Exercise, set: Set) {
        setSetArchived(exercise.id, set.id, false)
    }

    private suspend fun setSetArchived(exerciseId: UUID, setId: UUID, archived: Boolean) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val exObjId = findExerciseObjectId(tx, exercisesId, exerciseId) ?: return@use
                val setsId = tx.getListId(exObjId, "sets") ?: return@use
                val index = findSetIndex(tx, setsId, setId) ?: return@use
                val item = tx.get(setsId, index.toLong()).orElse(null) as? AmValue.Map ?: return@use
                tx.set(item.id, "archived", archived)
                tx.commit()
            }
            null
        }
    }

    private fun writeSet(tx: org.automerge.Transaction, objectId: ObjectId, set: Set) {
        tx.set(objectId, "id", set.id.toString())
        tx.setUint(objectId, "timestamp", set.timestamp.toEpochMilli())
        tx.set(objectId, "weight", set.weight)
        tx.set(objectId, "reps", set.reps)
        tx.set(objectId, "rpe", set.rpe)
        tx.set(objectId, "archived", false)
    }
}

fun readWorkouts(doc: Read, exerciseObjId: ObjectId, exercise: Exercise): List<Workout> {
    val setsId = doc.getListId(exerciseObjId, "sets") ?: return emptyList()
    val len = doc.length(setsId).toInt()
    val localTimeZone = ZonedDateTime.now().zone
    val sets = mutableListOf<Set>()
    for (i in 0 until len) {
        val item = doc.get(setsId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        val archived = doc.getBoolean(item.id, "archived") ?: false
        if (archived) continue
        val set = readSet(doc, item.id) ?: continue
        sets.add(set)
    }
    return sets
        .sortedByDescending { it.timestamp }
        .groupBy { LocalDateTime.ofInstant(it.timestamp, localTimeZone).toLocalDate() }
        .entries
        .sortedByDescending { it.key }
        .map { (date, dateSets) -> Workout(date, exercise, dateSets) }
}

fun readSet(doc: Read, objectId: ObjectId): Set? {
    val id = doc.getString(objectId, "id") ?: return null
    val timestamp = doc.getLong(objectId, "timestamp") ?: return null
    val weight = doc.getDouble(objectId, "weight") ?: return null
    val reps = doc.getDouble(objectId, "reps") ?: return null
    val rpe = doc.getDouble(objectId, "rpe") ?: return null
    return Set(
        UUID.fromString(id),
        weight,
        reps,
        rpe,
        Instant.ofEpochMilli(timestamp)
    )
}

fun findSetIndex(doc: Read, setsId: ObjectId, id: UUID): Int? {
    val idStr = id.toString()
    val len = doc.length(setsId).toInt()
    for (i in 0 until len) {
        val item = doc.get(setsId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        if (doc.getString(item.id, "id") == idStr) return i
    }
    return null
}
