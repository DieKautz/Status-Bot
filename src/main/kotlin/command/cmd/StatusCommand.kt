package command.cmd

import command.SlashCommand
import kotlinx.datetime.toJavaInstant
import org.javacord.api.entity.message.component.ActionRowBuilder
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import util.SeriesObserver
import java.awt.Color
import kotlin.time.ExperimentalTime


@ExperimentalTime
class StatusCommand : SlashCommand("status", "Displays current quest status.") {

    override fun handle(interaction: SlashCommandInteraction) {

        val relevantChallenge = SeriesObserver.getRelevant()

        val embed = EmbedBuilder()
            .setThumbnail("https://api.stellar.quest/badge/${relevantChallenge.badges.main}")
            .setAuthor("Current Status")
            .setTitle("`${SeriesObserver.getState().name}`")
            .setColor(Color.GREEN)


        val practiceBtn = Button.link("https://quest.stellar.org/play", "Practice Set 1-3", "🎯")
        val registerBtnBuilder = ButtonBuilder()
            .setUrl("https://quest.stellar.org/play/live")
            .setLabel("Register Now")
            .setEmoji("🕑")
            .setDisabled(true)
            .setStyle(ButtonStyle.LINK)
        val arBuilder = ActionRowBuilder()
            .addComponents(practiceBtn)

        when (SeriesObserver.getState()) {
            SeriesObserver.State.AWAITING_SERIES_START -> {
                embed.setColor(Color.YELLOW)
                    .setDescription(
                        "We are currently awaiting the start of series No.${SeriesObserver.currentSeriesNum} 🤗\n" +
                                "Its set to start at <t:${relevantChallenge.date.epochSeconds}>"
                    )
                    .setTimestamp(relevantChallenge.date.toJavaInstant())
            }
            SeriesObserver.State.WAITING_BETWEEN -> {
                embed.setColor(Color.YELLOW)
                    .setDescription("The previous challenge has already concluded. Next one will be at <t:${relevantChallenge.date.epochSeconds}:F>")
                    .setTimestamp(relevantChallenge.date.toJavaInstant())
            }
            SeriesObserver.State.REGISTRATION -> {
                registerBtnBuilder.setDisabled(false)
                arBuilder.addComponents(registerBtnBuilder.build())
                embed.setColor(Color.CYAN)
                    .setDescription("You can **register right now**. The challenge will start <t:${relevantChallenge.date.epochSeconds}:R>")
                    .setTimestamp(relevantChallenge.date.toJavaInstant())
            }
            SeriesObserver.State.RUNNING -> {
                embed.setColor(Color.GREEN)
                    .setDescription("**There is an LIVE Quest** right now, if you registered in time you can still compete!")
                    .setTimestamp(relevantChallenge.date.toJavaInstant())
                registerBtnBuilder
                    .setLabel("Play Now!")
                    .setEmoji("🚀")
                    .setDisabled(false)
                arBuilder.addComponents(registerBtnBuilder.build())
            }
            SeriesObserver.State.SERIES_CONCLUDED -> {
                embed.setColor(Color.ORANGE)
                    .setDescription("The last series concluded <t:${relevantChallenge.date.epochSeconds}:R> and there is no active challenge right now, but you can still practice with older sets.")
                    .setTimestamp(relevantChallenge.date.toJavaInstant())
            }
        }

        interaction.createImmediateResponder()
            .addEmbed(embed)
            .addComponents(arBuilder.build())
            .respond()
    }
}