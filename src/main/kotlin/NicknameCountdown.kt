import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until
import org.javacord.api.DiscordApi
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.server.Server
import org.slf4j.LoggerFactory
import util.SeriesObserver
import kotlin.concurrent.fixedRateTimer
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

    var lastExec: Instant = Instant.DISTANT_PAST

    fun forceUpdatePersona() {
        lastExec = Instant.DISTANT_PAST
        updatePersona()
    }

    private fun updatePersona() {
        val now = Clock.System.now()
        val nextName = "S${SeriesObserver.currentSeriesNum}Q${SeriesObserver.nextIndex()+1}"
        val lastName = "S${SeriesObserver.currentSeriesNum}Q${SeriesObserver.prevIndex()+1}"

        val diffToNext = SeriesObserver.getNext()?.date?.minus(now)
        val diffToPrev = SeriesObserver.getPrev()?.date?.let { now.minus(it) }
        val diffTime = diffToNext ?: diffToPrev!!

        if (diffTime.inWholeDays > 7 && lastExec.until(now, DateTimeUnit.HOUR) < 24
            || diffTime.inWholeHours > 48 && lastExec.until(now, DateTimeUnit.MINUTE) < 60) return
        val nickname = when(SeriesObserver.getState()) {
            SeriesObserver.State.AWAITING_SERIES_START, SeriesObserver.State.WAITING_BETWEEN -> {
                diffToNext!!.toComponents { days, hours, minutes, seconds, _ ->
                    api.unsetActivity()
                    "${days}d ${hours}h ${minutes}m ${seconds/10*10+10}s until $nextName"
                }
            }
            SeriesObserver.State.REGISTRATION -> {
                api.updateActivity(ActivityType.WATCHING, "registration")
                diffToNext!!.toComponents { minutes, seconds, _ ->
                    "${minutes}m ${seconds/10*10+10}s until lobby opening"
                }
            }
            SeriesObserver.State.RUNNING -> {
                api.updateActivity(ActivityType.COMPETING, lastName)
                diffToPrev!!.toComponents { hours, minutes, seconds, _ ->
                    "$nextName running (${hours}h ${minutes}m ${seconds/10*10+10}s)"
                }
            }
            SeriesObserver.State.SERIES_CONCLUDED -> {
                api.unsetActivity()
                "Series ${SeriesObserver.currentSeriesNum} has concluded!"
            }
        }
        lastExec = now
        api.yourself.updateNickname(srv, nickname)
    }
}