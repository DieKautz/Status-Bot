package command.cmd

import command.SlashCommand
import org.javacord.api.interaction.SlashCommandInteraction
import util.ActionRowHelper
import util.EmbedHelper
import util.SeriesObserver

class UpcomingCommand : SlashCommand("upcoming", "Displays the upcoming quest.") {

    override fun handle(interaction: SlashCommandInteraction) {
        val state = SeriesObserver.getState()

        if (state == SeriesObserver.State.SERIES_CONCLUDED) {
            interaction.createImmediateResponder()
                .setContent("\"The last series concluded <t:${SeriesObserver.getRelevant().date.epochSeconds}:R> " +
                        "and there is no active challenge right now, but you can still practice with older sets.\"")
                .respond()
            return
        }

        val nextChallenge = SeriesObserver.getNext()

        val responder = interaction.createImmediateResponder()
            .addEmbed(EmbedHelper.getEmbed(nextChallenge, state))
            .addComponents(ActionRowHelper.getActionRow(state))
        responder.respond()

        if (state == SeriesObserver.State.RUNNING) {
            interaction.createFollowupMessageBuilder()
                .setContent("But last quest is still active. Maybe there are some XLM left to earn.")
                .send()
        }
    }
}