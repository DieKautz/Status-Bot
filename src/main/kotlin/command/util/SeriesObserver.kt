package command.util

import command.util.series.Challenge
import command.util.series.Series
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager

object SeriesObserver {

    private val log = LogManager.getLogger(javaClass)

    var currentSeriesNum: Int = 4
    private lateinit var currentSeries: Series
    lateinit var challenges: List<Challenge>

    private val httpClient = HttpClient()
    fun fetchSeries(seriesEndpoint: String = "https://api.stellar.quest/utils/series?series=$currentSeriesNum") {
        log.info("Fetching series data from endpoint ($seriesEndpoint)")
        currentSeriesNum = Url(seriesEndpoint).parameters["series"]?.toInt()!!
        runBlocking {
            val response: String = httpClient.get(seriesEndpoint)
            val parsed = Json.decodeFromString<Series>(response)
            parsed.challenges.sortedBy { it.date }
            challenges = parsed.challenges
            currentSeries = parsed
            log.info("Successfully updated series! Loaded ${challenges.count()} in series No. $currentSeriesNum")
            log.info("NEW state is ${getState()}")
        }
    }

    fun getPrev(): Challenge? = challenges.lastOrNull { it.date < Clock.System.now() }
    fun getNext(): Challenge? = challenges.firstOrNull { it.date > Clock.System.now() }

    fun getState(): State {
        val now = Clock.System.now()
        if (now <= challenges.first().date) {
            return State.AWAITING_SERIES_START
        }
        if (now >= challenges.last().date) {
            return State.SERIES_CONCLUDED
        }
        val prevChallenge = getPrev()!!
        val nextChallenge = getNext()!!
        if (prevChallenge.date.until(now, DateTimeUnit.HOUR) <= 24) {
            return State.RUNNING
        }
        if (now.until(nextChallenge.date, DateTimeUnit.HOUR) <= 1) {
            return State.REGISTRATION
        }
        return State.WAITING_BETWEEN
    }

    enum class State {
        AWAITING_SERIES_START,
        WAITING_BETWEEN,
        REGISTRATION,
        RUNNING,
        SERIES_CONCLUDED,
    }
}