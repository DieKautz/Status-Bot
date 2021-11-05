package util

import org.javacord.api.entity.message.embed.EmbedBuilder
import util.SeriesObserver.State.*
import util.series.Challenge
import java.awt.Color

object EmbedHelper {
    fun getEmbed(challenge: Challenge, state: SeriesObserver.State): EmbedBuilder {
        val questNum = SeriesObserver.challenges.indexOf(challenge) + 1
        val questsCount = SeriesObserver.challenges.count()

        val unlockLobbyTime = challenge.date.epochSeconds

        val embed = EmbedBuilder()
            .setTitle("Quest ${questNum}")
            .setDescription(
                when (state) {
                    AWAITING_SERIES_START -> {
                        "We are waiting on the start of the ${SeriesObserver.currentSeriesNum.toOrdinal()} stellar quest! 🤗\n" +
                                "Its set to start <t:${unlockLobbyTime - 60 * 60}>"
                    }
                    WAITING_BETWEEN, REGISTRATION, RUNNING -> {
                        "This is the ${questNum.toOrdinal()} of $questsCount total quests in series ${SeriesObserver.currentSeriesNum}!"
                    }
                    SERIES_CONCLUDED -> {
                        "The last series concluded <t:$unlockLobbyTime:R> and there is no active challenge right now," +
                                "but you can still practice with older sets."
                    }
                }
            )
            .setThumbnail("https://api.stellar.quest/badge/${challenge.badges.main}")
            .setColor(
                when (state) {
                    AWAITING_SERIES_START, WAITING_BETWEEN -> Color(240, 173, 78)
                    REGISTRATION -> Color(91, 192, 222)
                    RUNNING -> Color(2, 117, 216)
                    SERIES_CONCLUDED -> Color(217, 83, 79)
                }
            )
        if (state != SERIES_CONCLUDED) {
            embed
                .addInlineField("Registration starts", "<t:${unlockLobbyTime - 60 * 60}:R>")
                .addInlineField("Lobbies unlock", "<t:$unlockLobbyTime:R>")
        }
        return embed
    }

    private fun Int.toOrdinal() = "$this" + if (this % 100 in 11..13) "th" else when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}