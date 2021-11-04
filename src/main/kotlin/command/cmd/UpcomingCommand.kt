package command.cmd

import command.SlashCommand
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.javacord.api.entity.message.component.ActionRowBuilder
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import util.SeriesObserver
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
class UpcomingCommand : SlashCommand("upcoming", "Displays upcoming quests.") {

    override fun handle(interaction: SlashCommandInteraction) {
        val nextIndex = SeriesObserver.challenges.indexOfFirst {
            it.date.epochSeconds > Clock.System.now().epochSeconds
        }
        val nextChallenge = SeriesObserver.getNext()
        val relevantChallenge = SeriesObserver.getRelevant()

        val embed = EmbedBuilder()
            .setAuthor("Next Up:")
            .setTitle("Quest ${nextIndex + 1}!")
            .setDescription("This challenge starts <t:${relevantChallenge.date.minus(Duration.Companion.hours(1)).epochSeconds}:R>")
            .setThumbnail("https://api.stellar.quest/badge/${relevantChallenge.badges.main}")
            .setTimestamp(relevantChallenge.date.minus(Duration.Companion.hours(1)).toJavaInstant())
        val practiceBtn = Button.link("https://quest.stellar.org/play", "Practice Set 1-3", "🎯")
        val registerBtnBuilder = ButtonBuilder()
            .setUrl("https://quest.stellar.org/play/live")
            .setLabel("Register Now")
            .setEmoji("🕑")
            .setDisabled(true)
            .setStyle(ButtonStyle.LINK)

        val arBuilder = ActionRowBuilder()

        when (SeriesObserver.getState()) {
            SeriesObserver.State.AWAITING_SERIES_START -> {
                arBuilder.addComponents(practiceBtn)
                embed.setDescription("We are currently awaiting the start of series No.${SeriesObserver.currentSeriesNum} 🤗")
            }
            SeriesObserver.State.WAITING_BETWEEN -> {
                arBuilder.addComponents(practiceBtn, registerBtnBuilder.build())
            }
            SeriesObserver.State.REGISTRATION -> {
                registerBtnBuilder
                    .setDisabled(false)
                    .setEmoji("🚀")
                arBuilder.addComponents(registerBtnBuilder.build())
                embed.setDescription("You can register now! The challenge will start <t:${relevantChallenge.date.epochSeconds}:R>")
            }
            SeriesObserver.State.RUNNING -> {
                arBuilder.addComponents(practiceBtn)
                registerBtnBuilder
                    .setDisabled(false)
                    .setEmoji("🚀")
                    .setLabel("Play Now!")
                arBuilder.addComponents(registerBtnBuilder.build())
                embed.setDescription(
                    "This challenge starts <t:${nextChallenge.date.minus(Duration.Companion.hours(1)).epochSeconds}:R>. \n" +
                            "But there is also one still running **right now** started <t:${relevantChallenge.date.epochSeconds}:R>"
                )
            }
            SeriesObserver.State.SERIES_CONCLUDED -> {
                arBuilder.addComponents(practiceBtn)
                embed.setTitle("NONE")
                embed.setDescription(
                    "The last series has sadly come to an end 😢.\n" +
                            "But you can still practice on the old sets in the mean time!"
                )
            }
        }

        val responder = interaction.createImmediateResponder()
            .addEmbed(embed)
        if (arBuilder.components.size > 0) {
            responder.addComponents(arBuilder.build())
        }
        responder.respond()
    }
}