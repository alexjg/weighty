package me.memoryandthought.weighty.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.memoryandthought.weighty.BuildConfig
import me.memoryandthought.weighty.database.ExerciseSetDTO
import java.time.Instant
import java.util.*

const val DATABASE_NAME = "weighty-db"

@Dao
interface ExerciseDao {
    @Insert(onConflict= OnConflictStrategy.ABORT)
    fun insertExercise(exercise: ExerciseDTO)

    @Query("SELECT * FROM exercise WHERE not archived")
    fun loadExercises(): LiveData<Array<ExerciseDTO>>

    @Query("SELECT * FROM exercise where id = :exerciseId")
    fun loadExercise(exerciseId: UUID): LiveData<ExerciseDTO?>

    @Update
    fun updateExercise(exercise: ExerciseDTO)

    @Insert
    fun insertAll(exercises: List<ExerciseDTO>)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * from ExerciseSetDTO where exerciseId = :exerciseId order by timestamp")
    fun exerciseSetsForExercise(exerciseId: UUID): LiveData<Array<ExerciseSetDTO>>
}

@Dao
interface SetDao {
    @Insert
    fun insertAll(sets: List<SetDTO>)
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

@Database(
    entities=arrayOf(ExerciseDTO::class, SetDTO::class),
    views=arrayOf(ExerciseSetDTO::class),
    exportSchema = false,
    version = 1
)
@TypeConverters(Converters::class)
abstract class WeightyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao

    companion object {
        @Volatile private var instance: WeightyDatabase? = null

        fun getInstance(context: Context): WeightyDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it}
            }
        }

        private fun buildDatabase(context: Context): WeightyDatabase {
            val builder = Room.databaseBuilder(context, WeightyDatabase::class.java, DATABASE_NAME)
            if (BuildConfig.PREPOPULATE_DATABASE) {
                builder.addCallback(object: RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                        WorkManager.getInstance().enqueue(request)
                    }
                })
            }
            return builder.build()
        }
    }
}
