package command.cmd

import command.SlashCommand
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import org.slf4j.LoggerFactory
import util.SeriesObserver

class RefetchCommand : SlashCommand("refetch", "Refetch current series data from this endpoint.") {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(interaction: SlashCommandInteraction) {
        log.warn("refetch requested from ${interaction.user.discriminatedName}")

        interaction.respondLater().thenAccept { interactionResponseUpdater ->
            interactionResponseUpdater
                .setContent("Fetching...")
                .update()

            runCatching {
                val url = interaction.firstOptionStringValue.get()
                SeriesObserver.fetchSeries(url)
                SeriesObserver.currentSeriesNum = interaction.secondOptionIntValue.get()
            }.onFailure {
                log.error("refetch failed with ${it.message}")
                interactionResponseUpdater
                    .setContent("**Refetch failed**: `${it.message}`")
                    .update()
            }.onSuccess {
                interactionResponseUpdater
                    .setContent("Successfully loaded ${SeriesObserver.challenges.count()} challenges of Series No.${SeriesObserver.currentSeriesNum} from `${interaction.firstOptionStringValue.get()}`")
                    .update()
            }
        }

    }

    override fun toSlashCommandBuilder() =
        super.toSlashCommandBuilder()
            .setDefaultPermission(false)
            .setOptions(
                listOf(
                    SlashCommandOption.create(
                        SlashCommandOptionType.STRING,
                        "url",
                        "Stellar Quest api endpoint to fetch current series data from.",
                        true
                    ),
                    SlashCommandOption.create(
                        SlashCommandOptionType.INTEGER,
                        "num",
                        "Number of the current series to fetch.",
                        true
                    )
                )
            )

}