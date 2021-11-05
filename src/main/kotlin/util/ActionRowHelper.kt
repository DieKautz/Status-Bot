package util

import org.javacord.api.entity.message.component.*
import util.SeriesObserver.State.*

object ActionRowHelper {
    fun getActionRow(state: SeriesObserver.State): ActionRow {
        val practiceBtn = Button.link("https://quest.stellar.org/play", "Practice Set 1-3", "🎯")
        val registerBtn = ButtonBuilder()
            .setUrl("https://quest.stellar.org/play/live")
            .setStyle(ButtonStyle.LINK)
            .setLabel("Register now")
            .setEmoji(if (state != REGISTRATION) "🕑" else "🚀")
            .setDisabled(state != REGISTRATION)
            .build()

        val playBtn = ButtonBuilder()
            .setUrl("https://quest.stellar.org/play/live")
            .setStyle(ButtonStyle.LINK)
            .setLabel("Play now!")
            .setEmoji("🚀")
            .setDisabled(state != RUNNING)
            .build()

        val arBuilder = ActionRowBuilder()
        when(state) {
            AWAITING_SERIES_START, WAITING_BETWEEN, REGISTRATION -> {
                arBuilder.addComponents(practiceBtn, registerBtn)
            }
            RUNNING -> {
                arBuilder.addComponents(practiceBtn, playBtn)
            }
            SERIES_CONCLUDED -> {
                arBuilder.addComponents(practiceBtn)
            }
        }

        return arBuilder.build()
    }
}