package util

import org.javacord.api.entity.message.component.*
import util.SeriesObserver.State.*

object ActionRowHelper {
    fun getActionRow(state: SeriesObserver.State): ActionRow {
        val practiceBtn = Button.link(
            "https://github.com/tyvdh/soroban-pioneer-quest",
            "Practice Pioneer Quest",
            "🥳"
        )

        val registerBtn = Button.link(
            "https://quest.stellar.org/live",
            "Prepare your Gitpod",
            if (state != REGISTRATION) "🕑" else "🚀"
        )

        val playBtn = Button.link(
            "https://quest.stellar.org/live",
            "Play now!",
            "🚀"
        )

        val arBuilder = ActionRowBuilder()
        when (state) {
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