package util

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.series.Challenge
import util.series.Series
import kotlin.time.Duration

object SeriesObserver {

    private val log = LoggerFactory.getLogger(javaClass)

    var currentFetchUrl: String = System.getenv("FETCH_URL")
    var currentSeriesNum: Int = 4
    private lateinit var currentSeries: Series
    lateinit var challenges: List<Challenge>

    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun fetchSeries(seriesEndpoint: String = currentFetchUrl) {
        log.info("Fetching series data from endpoint ($seriesEndpoint)")
        runBlocking {
            var response = ""
            runCatching {
                response = httpClient.get(seriesEndpoint)
            }.onFailure {
                throw IllegalArgumentException("Invalid URL provided!")
            }
            runCatching {
                val parsed = json.decodeFromString<Series>(response)
                parsed.challenges.sortedBy { it.date }
                challenges = parsed.challenges
                currentSeries = parsed
            }.onFailure {
                throw IllegalStateException("Invalid JSON at endpoint: ${it.message}")
            }
            currentFetchUrl = seriesEndpoint
            log.info("Successfully updated series! Loaded ${challenges.count()} in series No. $currentSeriesNum")
            log.info("NEW state is ${getState()}")
        }
    }

    private fun getPrev(): Challenge = challenges.last { it.date < Clock.System.now() }
    fun getNext(): Challenge = challenges.first { it.date > Clock.System.now() }

    fun getRelevantIndex(): Int = when(getState()) {
        State.RUNNING, State.SERIES_CONCLUDED -> challenges.indexOfLast { it.date < Clock.System.now() }
        else -> challenges.indexOfFirst { it.date > Clock.System.now() }
    }

    fun getRelevant() = when(getState()) {
        State.RUNNING, State.SERIES_CONCLUDED -> getPrev()
        else -> getNext()
    }

    fun getState(): State {
        val now = Clock.System.now()
        if (now <= challenges.first().date) {
            return State.AWAITING_SERIES_START
        }
        val prevChallenge = getPrev()
        if (prevChallenge.date.until(now, DateTimeUnit.HOUR) < 4) {
            return State.RUNNING
        }
        if (now >= challenges.last().date) {
            return State.SERIES_CONCLUDED
        }
        val nextChallenge = getNext()
        if (now.until(nextChallenge.date, DateTimeUnit.MINUTE) < 60) {
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