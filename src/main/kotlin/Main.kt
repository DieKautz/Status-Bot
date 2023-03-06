import command.SlashCommandHandler
import command.cmd.WatchLeaderboardCommand
import org.javacord.api.DiscordApiBuilder

fun main() {

    val api = DiscordApiBuilder()
        .setToken(System.getenv("BOT_TOKEN"))
        .login()
        .join()

    SlashCommandHandler(
        api, listOf(
            WatchLeaderboardCommand()
        )
    )
}
