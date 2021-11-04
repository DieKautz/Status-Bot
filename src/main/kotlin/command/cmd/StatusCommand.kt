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
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
class StatusCommand : SlashCommand("status", "Displays current quest status.") {

    override fun handle(interaction: SlashCommandInteraction) {

        val relevantChallenge = SeriesObserver.getRelevant()
        val registerTime = relevantChallenge.date.minus(Duration.Companion.hours(1))

        val embed = EmbedBuilder()
            .setThumbnail("https://api.stellar.quest/badge/${relevantChallenge.badges.main}")
            .setAuthor("Current Status")
            .setTitle("`${SeriesObserver.getState().name}`")
            .setColor(Color.GREEN)
            .setTimestamp(registerTime.toJavaInstant())


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
                                "Its set to start at <t:${registerTime.epochSeconds}>"
                    )
            }
            SeriesObserver.State.WAITING_BETWEEN -> {
                embed.setColor(Color.YELLOW)
                    .setDescription("The previous challenge has already concluded. Next one will be at <t:${registerTime.epochSeconds}:F>")
            }
            SeriesObserver.State.REGISTRATION -> {
                registerBtnBuilder.setDisabled(false)
                arBuilder.addComponents(registerBtnBuilder.build())
                embed.setColor(Color.CYAN)
                    .setDescription("You can **register right now**. The challenge will start <t:${registerTime.epochSeconds}:R>")
            }
            SeriesObserver.State.RUNNING -> {
                embed.setColor(Color.GREEN)
                    .setDescription("**There is an LIVE Quest** right now, if you registered in time you can still compete!")
                registerBtnBuilder
                    .setLabel("Play Now!")
                    .setEmoji("🚀")
                    .setDisabled(false)
                arBuilder.addComponents(registerBtnBuilder.build())
            }
            SeriesObserver.State.SERIES_CONCLUDED -> {
                embed.setColor(Color.ORANGE)
                    .setDescription("The last series concluded <t:${registerTime.epochSeconds}:R> and there is no active challenge right now, but you can still practice with older sets.")
            }
        }

        interaction.createImmediateResponder()
            .addEmbed(embed)
            .addComponents(arBuilder.build())
            .respond()
    }
}