package me.memoryandthought.weighty.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.automerge.AmValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Read
import org.automerge.kotlin.getBoolean
import org.automerge.kotlin.getListId
import org.automerge.kotlin.getLong
import org.automerge.kotlin.getString
import org.automerge.kotlin.observe
import org.automerge.kotlin.withDocAsync
import org.automerge.repo.DocHandle
import me.memoryandthought.weighty.domain.Exercise
import java.time.Instant
import java.util.UUID

interface ExerciseRepository {
    fun loadExercises(scope: CoroutineScope): StateFlow<List<Exercise>>
    fun loadExercise(scope: CoroutineScope, id: UUID): StateFlow<Exercise?>
    suspend fun addExercise(name: String)
    suspend fun updateExercise(ex: Exercise)
    suspend fun archiveExercise(ex: Exercise)
    suspend fun unarchiveExercise(ex: Exercise)
}

class ExerciseRepositoryImpl(private val handle: DocHandle) : ExerciseRepository {

    override fun loadExercises(scope: CoroutineScope): StateFlow<List<Exercise>> {
        return handle.observe(scope, emptyList()) { doc ->
            readExercises(doc)
        }
    }

    override fun loadExercise(scope: CoroutineScope, id: UUID): StateFlow<Exercise?> {
        return handle.observe(scope, null) { doc ->
            val exercisesId = doc.getListId(ObjectId.ROOT, "exercises") ?: return@observe null
            val len = doc.length(exercisesId).toInt()
            for (i in 0 until len) {
                val item = doc.get(exercisesId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
                val exId = doc.getString(item.id, "id") ?: continue
                if (exId == id.toString()) {
                    return@observe readExercise(doc, item.id)
                }
            }
            null
        }
    }

    override suspend fun addExercise(name: String) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val len = tx.length(exercisesId)
                val itemId = tx.insert(exercisesId, len, ObjectType.MAP)
                tx.set(itemId, "id", UUID.randomUUID().toString())
                tx.set(itemId, "name", name)
                tx.setUint(itemId, "created", Instant.now().toEpochMilli())
                tx.set(itemId, "archived", false)
                tx.set(itemId, "sets", ObjectType.LIST)
                tx.commit()
            }
            null
        }
    }

    override suspend fun updateExercise(ex: Exercise) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val index = findExerciseIndex(tx, exercisesId, ex.id) ?: return@use
                val item = tx.get(exercisesId, index.toLong()).orElse(null) as? AmValue.Map ?: return@use
                tx.set(item.id, "name", ex.name)
                tx.commit()
            }
            null
        }
    }

    override suspend fun archiveExercise(ex: Exercise) {
        setArchived(ex.id, true)
    }

    override suspend fun unarchiveExercise(ex: Exercise) {
        setArchived(ex.id, false)
    }

    private suspend fun setArchived(id: UUID, archived: Boolean) {
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                val exercisesId = tx.getListId(ObjectId.ROOT, "exercises")!!
                val index = findExerciseIndex(tx, exercisesId, id) ?: return@use
                val item = tx.get(exercisesId, index.toLong()).orElse(null) as? AmValue.Map ?: return@use
                tx.set(item.id, "archived", archived)
                tx.commit()
            }
            null
        }
    }
}

fun readExercises(doc: Read): List<Exercise> {
    val exercisesId = doc.getListId(ObjectId.ROOT, "exercises") ?: return emptyList()
    val len = doc.length(exercisesId).toInt()
    val exercises = mutableListOf<Pair<Exercise, Instant?>>()
    for (i in 0 until len) {
        val item = doc.get(exercisesId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        val archived = doc.getBoolean(item.id, "archived") ?: false
        if (archived) continue
        val exercise = readExercise(doc, item.id) ?: continue
        val lastSetTimestamp = mostRecentSetTimestamp(doc, item.id)
        exercises.add(exercise to lastSetTimestamp)
    }
    return exercises
        .sortedByDescending { it.second ?: Instant.MIN }
        .map { it.first }
}

fun readExercise(doc: Read, objectId: ObjectId): Exercise? {
    val id = doc.getString(objectId, "id") ?: return null
    val name = doc.getString(objectId, "name") ?: return null
    val created = doc.getLong(objectId, "created") ?: return null
    return Exercise(UUID.fromString(id), name, Instant.ofEpochMilli(created))
}

fun mostRecentSetTimestamp(doc: Read, exerciseObjectId: ObjectId): Instant? {
    val setsId = doc.getListId(exerciseObjectId, "sets") ?: return null
    val len = doc.length(setsId).toInt()
    var latest: Long? = null
    for (i in 0 until len) {
        val item = doc.get(setsId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        val archived = doc.getBoolean(item.id, "archived") ?: false
        if (archived) continue
        val ts = doc.getLong(item.id, "timestamp") ?: continue
        if (latest == null || ts > latest) latest = ts
    }
    return latest?.let { Instant.ofEpochMilli(it) }
}

fun findExerciseIndex(doc: Read, exercisesId: ObjectId, id: UUID): Int? {
    val idStr = id.toString()
    val len = doc.length(exercisesId).toInt()
    for (i in 0 until len) {
        val item = doc.get(exercisesId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        if (doc.getString(item.id, "id") == idStr) return i
    }
    return null
}

fun findExerciseObjectId(doc: Read, exercisesId: ObjectId, id: UUID): ObjectId? {
    val idStr = id.toString()
    val len = doc.length(exercisesId).toInt()
    for (i in 0 until len) {
        val item = doc.get(exercisesId, i.toLong()).orElse(null) as? AmValue.Map ?: continue
        if (doc.getString(item.id, "id") == idStr) return item.id
    }
    return null
}
