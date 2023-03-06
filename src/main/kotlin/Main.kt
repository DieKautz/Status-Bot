import command.SlashCommandHandler
import command.cmd.RefetchCommand
import command.cmd.StatusCommand
import command.cmd.UpcomingCommand
import org.javacord.api.DiscordApiBuilder
import util.SeriesObserver

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

    val srv = api.getServerById(System.getenv("TIMER_SERVER_ID")).get()
    NicknameCountdown.startTimer(srv)
}
