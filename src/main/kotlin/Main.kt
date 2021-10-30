import command.SlashCommandHandler
import command.cmd.RefetchCommand
import command.cmd.StatusCommand
import command.cmd.UpcomingCommand
import command.util.SeriesObserver
import org.javacord.api.DiscordApiBuilder
import kotlin.time.ExperimentalTime

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
