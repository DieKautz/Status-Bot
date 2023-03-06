import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.javacord.api.DiscordApi
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.server.Server
import org.tinylog.kotlin.Logger
import util.SeriesObserver
import util.SeriesObserver.State.*
import java.time.temporal.ChronoUnit
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object NicknameCountdown {
    private lateinit var api: DiscordApi
    private lateinit var srv: Server

    fun startTimer(srv: Server) {
        this.srv = srv
        this.api = srv.api

        forceUpdatePersona()

        // update persona periodically (starting on multiples of 10sec)
        val now = Clock.System.now().toJavaInstant()
        val nextMinute = now.truncatedTo(ChronoUnit.MINUTES).plus(ChronoUnit.MINUTES.duration)
        fixedRateTimer("nick renamer", true, now.until(nextMinute, ChronoUnit.MILLIS) / 6, 1000) {
            updatePersona()
        }
    }

    private var lastExec = 0L
    private var lastState = SERIES_CONCLUDED

    fun forceUpdatePersona() {
        lastExec = 0
        updatePersona()
    }

    private fun updatePersona() {
        val now = Clock.System.now()
        val nextName = "S${SeriesObserver.currentSeriesNum}Q${SeriesObserver.getRelevantIndex() + 1}"

        val diffToRelevant = now.epochSeconds - SeriesObserver.getRelevant().date.epochSeconds
        val diffTime = ((diffToRelevant) / 10 * 10).seconds.absoluteValue

        val currState = SeriesObserver.getState()
        if (currState != lastState) {
            Logger.info("state is now $currState")
            lastExec = 0
        }

        // cap nickname refreshes
        if (diffTime.inWholeMinutes > 120 && (now.epochSeconds - lastExec) < 60 // refresh only every min when >2 hours
            || diffTime.inWholeSeconds > 120 && (now.epochSeconds - lastExec) < 30 // refresh only every 30 sec when >2 minutes
            || (now.epochSeconds - lastExec) < 10 // refresh only every 10 sec at most
        ) {
            return
        }
        lastExec = now.epochSeconds

        val nickname = when (SeriesObserver.getState()) {
            AWAITING_SERIES_START, WAITING_BETWEEN -> {
                if (lastState != currState) api.updateActivity(ActivityType.LISTENING, "до $nextName")
                diffTime.minus(1.hours).toString()
                diffTime.minus(1.hours).toComponents { days, hours, minutes,  _, _ -> "${days}d ${hours}h ${minutes}m" }
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