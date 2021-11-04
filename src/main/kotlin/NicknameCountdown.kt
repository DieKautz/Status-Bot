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
        fixedRateTimer("nick renamer", true, 0, 1000) {
            updatePersona()
        }
    }

    var lastExec = 0L
    var lastState = SERIES_CONCLUDED

    fun forceUpdatePersona() {
        lastExec = 0
        updatePersona()
    }

    private fun updatePersona() {
        val now = Clock.System.now()
        val nextName = "S${SeriesObserver.currentSeriesNum}Q${SeriesObserver.nextIndex()+1}"

        val diffToRelevant = now.epochSeconds - SeriesObserver.getRelevant().date.epochSeconds
        val diffTime = Duration.Companion.seconds((diffToRelevant)/10*10).absoluteValue

        val currState = SeriesObserver.getState()
        if (currState != lastState) {
            log.info("state is now $currState")
            lastExec = 0
        }

        // cap nickname refreshes
        if (diffTime.inWholeDays > 7 && (now.epochSeconds - lastExec) < 24*60*60 // refresh only daily when >7days
                    || diffTime.inWholeHours > 48 && (now.epochSeconds - lastExec) < 60*60 // refresh only hourly when >2 days
                    || diffTime.inWholeMinutes > 120 && (now.epochSeconds - lastExec) < 60*5 // refresh only every 5 min when >2 hours
                    || diffTime.inWholeSeconds > 120 && (now.epochSeconds - lastExec) < 30 // refresh only every 30 sec when >2 mins
                    || (now.epochSeconds - lastExec) < 10 // refresh only every 10 sec at most
        ) {return}
        lastExec = now.epochSeconds

        val nickname = when(SeriesObserver.getState()) {
            AWAITING_SERIES_START, WAITING_BETWEEN -> {
                if (lastState != currState) api.updateActivity(ActivityType.CUSTOM, "until $nextName")
                diffTime.toString()
            }
            REGISTRATION -> {
                if (lastState != currState) api.updateActivity(ActivityType.WATCHING, "registration")
                "$diffTime until lobby opening"
            }
            RUNNING -> {
                if (lastState != currState) api.updateActivity(ActivityType.COMPETING, nextName)
                val timeString = diffTime.toComponents { hours, minutes, _, _ -> "${hours}h ${minutes}m" }
                "\uD83D\uDD34 LIVE ($timeString)"
            }
            SERIES_CONCLUDED -> {
                if (lastState != currState) api.unsetActivity()
                "Series ${SeriesObserver.currentSeriesNum} has concluded!"
            }
        }
        api.yourself.updateNickname(srv, nickname)
        lastState = currState
    }
}