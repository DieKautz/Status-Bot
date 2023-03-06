package command.cmd

import NicknameCountdown
import command.SlashCommand
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import org.tinylog.kotlin.Logger
import util.SeriesObserver

class RefetchCommand : SlashCommand("refetch", "Refetch current series data from this endpoint.") {

    override fun handle(interaction: SlashCommandInteraction) {
        Logger.warn("refetch requested from ${interaction.user.discriminatedName}")


        interaction.createImmediateResponder()
            .setContent("Fetching...")
            .setFlags(MessageFlag.EPHEMERAL)
            .respond().thenAccept { interactionResponseUpdater ->

                runCatching {
                    val url = interaction.getArgumentStringValueByName("url").get()
                    SeriesObserver.fetchSeries(url)
                    SeriesObserver.currentSeriesNum = interaction.getArgumentLongValueByName("num").get().toInt()
                }.onFailure {
                    Logger.error("refetch failed with ${it.message}")
                    interactionResponseUpdater
                        .setContent("**Refetch failed**: `${it.message}`")
                        .update()
                }.onSuccess {
                    interactionResponseUpdater
                        .setContent(
                            "Successfully loaded ${SeriesObserver.challenges.count()} challenges of series ${SeriesObserver.currentSeriesNum} from `${
                                interaction.getArgumentStringValueByName("url").get()
                            }`"
                        )
                        .update()
                    NicknameCountdown.forceUpdatePersona()
                }
            }

    }

    override fun toSlashCommandBuilder() =
        super.toSlashCommandBuilder()
            .setDefaultDisabled()
            .setDefaultEnabledForPermissions(PermissionType.MODERATE_MEMBERS)
            .addOption(
                SlashCommandOption.create(
                    SlashCommandOptionType.STRING,
                    "url",
                    "Stellar Quest api endpoint to fetch current series data from.",
                    true
                )
            )
            .addOption(
                SlashCommandOption.create(
                    SlashCommandOptionType.LONG,
                    "num",
                    "Number of the current series to fetch.",
                    true
                )
            )
}