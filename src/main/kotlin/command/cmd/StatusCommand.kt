package command.cmd

import command.SlashCommand
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.SlashCommandInteraction
import util.SeriesObserver


class StatusCommand : SlashCommand("status", "Displays current quest status.") {

    override fun handle(interaction: SlashCommandInteraction) {
        val relevantChallenge = SeriesObserver.getRelevant()
        val state = SeriesObserver.getState()
        val questNum = SeriesObserver.challenges.indexOf(relevantChallenge) + 1
        val questsCount = SeriesObserver.challenges.count()
        val fetchUrl = SeriesObserver.currentFetchUrl

        interaction.createImmediateResponder()
            .setContent("current state is `$state` for quest `$questNum/$questsCount` in series ${SeriesObserver.currentSeriesNum}\n" +
                    "fetching from $fetchUrl")
            .setFlags(MessageFlag.EPHEMERAL)
            .respond()
    }

    override fun toSlashCommandBuilder() =
        super.toSlashCommandBuilder()
            .setDefaultDisabled()
}