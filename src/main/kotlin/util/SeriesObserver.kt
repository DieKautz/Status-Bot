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

    fun getPrev(): Challenge? = challenges.lastOrNull { it.date < Clock.System.now() }
    fun getNext(): Challenge? = challenges.firstOrNull { it.date > Clock.System.now() }

    fun prevIndex(): Int = challenges.indexOfLast { it.date < Clock.System.now() }
    fun nextIndex(): Int = challenges.indexOfFirst { it.date > Clock.System.now() }

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
        if (prevChallenge.date.until(now, DateTimeUnit.HOUR) < 24) {
            return State.RUNNING
        }
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