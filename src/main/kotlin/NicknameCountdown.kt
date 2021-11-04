import kotlinx.datetime.Clock
import org.javacord.api.DiscordApi
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.server.Server
import org.slf4j.LoggerFactory
import util.SeriesObserver
import util.SeriesObserver.State.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object NicknameCountdown {

    private val log = LoggerFactory.getLogger(javaClass)
    lateinit var api: DiscordApi
    lateinit var srv: Server

    fun startTimer(srv: Server) {
        this.srv = srv
        this.api = srv.api
        fixedRateTimer("nick renamer", true, 0, 30000) {
            updatePersona()
        }
    }

    var lastExec = 0L
    var lastState = SeriesObserver.getState()

    fun forceUpdatePersona() {
        lastExec = 0
        updatePersona()
    }

    private fun updatePersona() {
        val now = Clock.System.now()
        val nextName = "S${SeriesObserver.currentSeriesNum}Q${SeriesObserver.nextIndex()+1}"

        val diffToNext = SeriesObserver.getNext()?.let { it.date.epochSeconds - now.epochSeconds }
        val diffToPrev = SeriesObserver.getPrev()?.let { now.epochSeconds - it.date.epochSeconds }
        val diffTime = Duration.Companion.seconds((diffToNext ?: diffToPrev!!)/10*10)

        val newState = SeriesObserver.getState()
        if (newState != lastState) {
            log.info("state is now $newState")
            lastState = newState
            lastExec = 0
        }
        if (diffTime.inWholeDays > 7 && (lastExec - now.epochSeconds) < 24*60*60 // refresh only hourly when >7days
            || diffTime.inWholeHours > 48 && (lastExec - now.epochSeconds) < 24*60*60) return // refresh only every minute when >1hour
        val nickname = when(SeriesObserver.getState()) {
            AWAITING_SERIES_START, WAITING_BETWEEN -> {
                if (api.activity.isPresent) api.unsetActivity()
                "$diffTime until $nextName"
            }
            REGISTRATION -> {
                api.updateActivity(ActivityType.WATCHING, "registration")
                "$diffTime until lobby opening"
            }
            RUNNING -> {
                "$nextName running ($diffTime)"
            }
            SERIES_CONCLUDED -> {
                if (api.activity.isPresent) api.unsetActivity()
                "Series ${SeriesObserver.currentSeriesNum} has concluded!"
            }
        }
        lastExec = now.epochSeconds
        api.yourself.updateNickname(srv, nickname)
    }
}