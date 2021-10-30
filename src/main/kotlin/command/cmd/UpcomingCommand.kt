package command.cmd

import command.SlashCommand
import command.util.SeriesObserver
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.javacord.api.entity.message.component.ActionRowBuilder
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import kotlin.time.ExperimentalTime


@ExperimentalTime
class UpcomingCommand : SlashCommand("upcoming", "Displays upcoming quests.") {

    override fun handle(interaction: SlashCommandInteraction) {
        val nextIndex = SeriesObserver.challenges.indexOfFirst {
            it.date.epochSeconds > Clock.System.now().epochSeconds
        }
        val nextChallenge = SeriesObserver.getNext() ?: SeriesObserver.challenges.first()

        val embed = EmbedBuilder()
            .setAuthor("Next Up:")
            .setTitle("Quest ${nextIndex + 1}!")
            .setDescription("This challenge starts <t:${nextChallenge.date.epochSeconds}:R>")
            .setThumbnail("https://api.stellar.quest/badge/${nextChallenge.badges.main}")
            .setTimestamp(nextChallenge.date.toJavaInstant())
        val practiceBtn = Button.link("https://quest.stellar.org/play", "Practice Set 1-3", "🎯")
        val registerBtnBuilder = ButtonBuilder()
            .setUrl("https://quest.stellar.org/play/live")
            .setLabel("Register Now")
            .setEmoji("🕑")
            .setDisabled(true)
            .setStyle(ButtonStyle.LINK)

        var showRegisterBtn = false
        var showPracticeBtn = true

        when (SeriesObserver.getState()) {
            SeriesObserver.State.AWAITING_SERIES_START -> {
                embed.setDescription("We are currently awaiting the start on series No.${SeriesObserver.currentSeriesNum} 🤗")
            }
            SeriesObserver.State.WAITING_BETWEEN -> {
                showRegisterBtn = true
            }
            SeriesObserver.State.REGISTRATION -> {
                showPracticeBtn = false
                showRegisterBtn = true
                registerBtnBuilder.setDisabled(false)
                registerBtnBuilder.setEmoji("🚀")
                embed.addField("", "You can register now!")
            }
            SeriesObserver.State.RUNNING -> {
                showPracticeBtn = false
                embed.addField("", "(There is also one running *right now*!)")
            }
            SeriesObserver.State.SERIES_CONCLUDED -> {
                embed.setTitle("NONE")
                embed.setDescription(
                    "The last series has sadly come to an end 😢.\n" +
                            "But you can still practice on the old sets in the mean time! ☺️"
                )
            }
        }
        val arBuilder = ActionRowBuilder()
        if (showPracticeBtn) arBuilder.addComponents(practiceBtn)
        if (showRegisterBtn) arBuilder.addComponents(registerBtnBuilder.build())

        interaction.createImmediateResponder()
            .addEmbed(embed)
            .addComponents(arBuilder.build())
            .respond()
    }
}