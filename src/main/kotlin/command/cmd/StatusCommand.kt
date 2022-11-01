package command.cmd

import command.SlashCommand
import org.javacord.api.interaction.SlashCommandInteraction
import util.ActionRowHelper
import util.EmbedHelper
import util.SeriesObserver


class StatusCommand : SlashCommand("status", "Displays current quest status.") {

    override fun handle(interaction: SlashCommandInteraction) {
        val relevantChallenge = SeriesObserver.getRelevant()
        val state = SeriesObserver.getState()
        val questNum = SeriesObserver.challenges.indexOf(relevantChallenge) + 1
        val questsCount = SeriesObserver.challenges.count()

        interaction.createImmediateResponder()
            .setContent("current state is `$state` for quest `$questNum/$questsCount` in series ${SeriesObserver.currentSeriesNum}")
            .addEmbed(EmbedHelper.getEmbed(relevantChallenge, state))
            .addComponents(ActionRowHelper.getActionRow(state))
            .respond()
    }

    override fun toSlashCommandBuilder() =
        super.toSlashCommandBuilder()
            .setDefaultDisabled()
}