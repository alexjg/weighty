package me.memoryandthought.weighty.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.Instant
import java.util.*

const val DATABASE_NAME = "weighty-db"

@Dao
interface ExerciseDao {
    @Insert(onConflict= OnConflictStrategy.ABORT)
    fun insertExercise(exercise: ExerciseDTO)

    @Query("SELECT * FROM exercise WHERE not archived")
    fun loadExercises(): LiveData<Array<ExerciseDTO>>

    @Update
    fun updateExercise(exercise: ExerciseDTO)

}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it)}
    }

    @TypeConverter
    fun toTimestamp(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun fromUuid(value: UUID?): String? = value?.let { it.toString() }

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let { UUID.fromString(it)}
}

@Database(entities=arrayOf(ExerciseDTO::class), exportSchema = false, version = 1)
@TypeConverters(Converters::class)
abstract class WeightyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile private var instance: WeightyDatabase? = null

        fun getInstance(context: Context): WeightyDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it}
            }
        }

        private fun buildDatabase(context: Context): WeightyDatabase {
            return Room.databaseBuilder(context, WeightyDatabase::class.java, DATABASE_NAME).build()
        }
    }
}
