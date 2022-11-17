package command.cmd

import NicknameCountdown
import command.SlashCommand
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import org.slf4j.LoggerFactory
import util.SeriesObserver

class RefetchCommand : SlashCommand("refetch", "Refetch current series data from this endpoint.") {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(interaction: SlashCommandInteraction) {
        log.warn("refetch requested from ${interaction.user.discriminatedName}")


        interaction.createImmediateResponder()
            .setContent("Fetching...")
            .setFlags(MessageFlag.EPHEMERAL)
            .respond().thenAccept { interactionResponseUpdater ->

                runCatching {
                    val url = interaction.getOptionStringValueByIndex(0).get()
                    SeriesObserver.fetchSeries(url)
                    SeriesObserver.currentSeriesNum = interaction.getOptionLongValueByIndex(1).get().toInt()
                }.onFailure {
                    log.error("refetch failed with ${it.message}")
                    interactionResponseUpdater
                        .setContent("**Refetch failed**: `${it.message}`")
                        .update()
                }.onSuccess {
                    interactionResponseUpdater
                        .setContent(
                            "Successfully loaded ${SeriesObserver.challenges.count()} challenges of series ${SeriesObserver.currentSeriesNum} from `${
                                interaction.getOptionStringValueByIndex(
                                    0
                                ).get()
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