package orm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class LeaderboardEntry(
    @SerialName("bucket_id") val bucketId: Int,
    @SerialName("task_id") val taskId: Int,
    @SerialName("CPU") val cpuCycles: Long,
    @SerialName("MEM") val memoryBytes: Long,
    @SerialName("SIZE") val fileBytes: Long,
    @SerialName("submission_date") val submissionDate: Long,
    @SerialName("rank") val rank: Int,
    @SerialName("anon_index") val anonIndex: Int? = null,
    @SerialName("display") val displayName: String? = null,
    @SerialName("profile_url") val profileUrl: String? = null,
    @SerialName("live") val live: Boolean,
) {
    val name: String
        get() = displayName ?: "anon [$anonIndex]"
    val nameWithEmoji: String
        get() = prefixEmoji() + name

    fun isSame(other: LeaderboardEntry): Boolean {
        if (bucketId != other.bucketId) return false
        if (taskId != other.taskId) return false
        if (live != other.live) return false
        if (name != other.name) return false

        return true
    }

    private fun prefixEmoji() = when (rank) {
        1 -> "🏆 "
        2 -> "🥈 "
        3 -> "🥉 "
        else -> "${rank}th "
    }

    fun displayString(): String {
        return """
            📆 <t:${submissionDate.milliseconds.inWholeSeconds}:R>
            🚀 ${"%.2f".format(cpuCycles/1_000_000.0)} M
            💾 $fileBytes Bytes
        """.trimIndent()
    }
}
