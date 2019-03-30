package me.memoryandthought.weighty.database

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import me.memoryandthought.weighty.domain.Exercise
import java.lang.reflect.Type
import java.time.Instant


class InstantConverter : TypeAdapter<Instant>() {
    override fun read(reader: JsonReader?): Instant? {
        reader?.let {
            if (it.peek() == JsonToken.NULL) {
                it.nextNull()
                return null
            }
            return Instant.parse(it.nextString())
        } ?: throw IllegalStateException("Invalid reader")
    }

    override fun write(out: JsonWriter?, value: Instant?) {
        out?.let {
            if (value == null){
                it.nullValue()
                return
            }
            out.value(value.toString())
        }
    }
}


class SeedDatabaseWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val TAG by lazy { SeedDatabaseWorker::class.java.simpleName }
    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork(): Result {
        val gson = GsonBuilder().registerTypeAdapter(Instant::class.java, InstantConverter()).create()
        try {
            applicationContext.assets.open("exercises.json").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val exerciseType: Type = object : TypeToken<List<ExerciseDTO>>(){}.type
                    val exerciseList: List<ExerciseDTO> = gson.fromJson(jsonReader, exerciseType)

                    val database = WeightyDatabase.getInstance(applicationContext)
                    database.exerciseDao().insertAll(exerciseList)
                }
            }
            applicationContext.assets.open("sets.json").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val setType: Type = object: TypeToken<List<SetDTO>>(){}.type
                    val setList: List<SetDTO> = gson.fromJson(jsonReader, setType)

                    val database = WeightyDatabase.getInstance(applicationContext)
                    database.setDao().insertAll(setList)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database", e)
            return Result.failure()
        }
    }
}