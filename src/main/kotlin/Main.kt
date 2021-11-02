import command.SlashCommandHandler
import command.cmd.RefetchCommand
import command.cmd.StatusCommand
import command.cmd.UpcomingCommand
import org.javacord.api.DiscordApiBuilder
import org.slf4j.LoggerFactory
import util.SeriesObserver
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger("MAIN")

@OptIn(ExperimentalTime::class)
fun main() {

    val api = DiscordApiBuilder()
        .setToken(System.getenv("BOT_TOKEN"))
        .login()
        .join()

    SeriesObserver.fetchSeries()

    SlashCommandHandler(
        api, listOf(
            StatusCommand(),
            UpcomingCommand(),
            RefetchCommand(),
        )
    )
}
