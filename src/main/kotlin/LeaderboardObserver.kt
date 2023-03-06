import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.component.ActionRowBuilder
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.tinylog.kotlin.Logger
import orm.LeaderboardEntry
import util.LeaderboardUpdate
import java.io.InputStream
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.seconds

class LeaderboardObserver(
    private val taskId: Long,
    private val bucketId: Long,
    private val name: String,
    private val announceThreshold: Long = 5,
    private val announceChannel: ServerTextChannel,
    private val live: Boolean = true,
) {
    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    val server: Server
        get() = announceChannel.server

    private var lastLeaderboard: List<LeaderboardEntry> = listOf()
    private var timer: Timer? = null

    fun startTimer() {
        if (timer != null) {
            Logger.warn("Attempted to start timer for $taskId, $bucketId a second time!")
            return
        }
        timer = fixedRateTimer(javaClass.name, true, 0, 10.seconds.inWholeMilliseconds) {
            runBlocking {
                checkLeaderboard()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun checkLeaderboard() {
        lateinit var bodyIs: InputStream
        val updates = mutableListOf<LeaderboardUpdate>()
        runCatching {
            bodyIs = httpClient.get(LEADERBOARD_ENDPOINT) {
                url {
                    parameters.append("task", taskId.toString())
                    parameters.append("bucket", bucketId.toString())
                    parameters.append("live", live.toString())
                    parameters.append("limit", 100.toString())
                }
            }.bodyAsChannel().toInputStream()
        }.onFailure { throw IllegalArgumentException("Cannot fetch leaderboard!") }
        runCatching {
            val leaderboard = json.decodeToSequence<LeaderboardEntry>(bodyIs).toList().sortedBy { it.rank }
            leaderboard.take(announceThreshold.toInt()).forEach { entry ->
                val oldRank = lastLeaderboard.find { it.isSame(entry) }?.rank
                if (oldRank == null || oldRank < entry.rank) {
                    updates += LeaderboardUpdate(entry, oldRank)
                }
            }
            lastLeaderboard = leaderboard
        }.onFailure { throw IllegalStateException("Cannot parse response from leaderboard endpoint", it) }
            .onSuccess { if(updates.isNotEmpty()) announceUpdates(updates) }
    }

    private fun announceUpdates(updates: List<LeaderboardUpdate>) {
        val embed = EmbedBuilder()
            .setTitle("Updates on \"$name\"")
            .setTimestampToNow()
        if (updates.size == 1 && updates[0].entry.profileUrl != null) {
            embed.setUrl(updates[0].entry.profileUrl)
        }
        val buttons = ActionRowBuilder()
            .addComponents(Button.link(GAME_URL, "⚔️"))
        val title = updateTitle(updates)
        updates.forEach { embed.addInlineField(it.entry.nameWithEmoji, it.entry.displayString()) }
        announceChannel.sendMessage(title, embed, buttons.build())
        updates.firstOrNull { it.entry.rank <= 3 }?.let {
            announceChannel.api.yourself.updateNickname(announceChannel.server, it.entry.nameWithEmoji)
            announceChannel.api.updateActivity(ActivityType.COMPETING, name)
        }
    }

    private fun updateTitle(updates: List<LeaderboardUpdate>): String {
        updates.firstOrNull { it.entry.rank == 1 }?.let {
            return "${it.entry.name} took the lead!"
        }
        updates.firstOrNull { it.entry.rank == 2 }?.let {
            val first = lastLeaderboard[0].name
            return "${it.entry.name} is getting very close to $first!"
        }
        return updates.joinToString(", ") { it.entry.name } + " joined the top $announceThreshold"
    }

    override fun toString(): String = "\"$name\" in ${announceChannel.mentionTag} (task=$taskId, bucket=$bucketId live=$live)"

    companion object {
        val LEADERBOARD_ENDPOINT: String = System.getenv("LEADERBOARD_ENDPOINT")
        val GAME_URL: String = System.getenv("GAME_URL")
    }
}