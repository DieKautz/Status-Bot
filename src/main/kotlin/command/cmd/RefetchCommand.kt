package command.cmd

import command.SlashCommand
import command.util.SeriesObserver
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType

class RefetchCommand : SlashCommand("refetch", "Refetch current series data from this endpoint.") {
    override fun handle(interaction: SlashCommandInteraction) {

        interaction.respondLater().thenAccept {
            it.setContent("Fetching...").update()

            runCatching {
                val url = interaction.firstOptionStringValue.get()
                SeriesObserver.fetchSeries(url)
            }.onFailure {
                interaction.createFollowupMessageBuilder()
                    .setContent("Failed! `${it.message}`")
                    .send()
            }.onSuccess {
                interaction.createFollowupMessageBuilder()
                    .setContent("Successfully updated fetch URL to Series No.${SeriesObserver.currentSeriesNum}")
                    .send()
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
                    )
                )
            )

}