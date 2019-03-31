package me.memoryandthought.weighty.domain
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class Exercise(val id: UUID, val name: String, val created: Instant) : Parcelable
@Parcelize
data class Set(val id: UUID, val weight: Double, val reps: Double, val rpe: Double, val timestamp: Instant) : Parcelable
data class Workout(val date: LocalDate, val exercise: Exercise, val sets: List<Set>)
