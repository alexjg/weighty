package me.memoryandthought.weighty
import java.time.Instant
import java.util.UUID

data class Exercise(val id: UUID, val name: String, val created: Instant)
