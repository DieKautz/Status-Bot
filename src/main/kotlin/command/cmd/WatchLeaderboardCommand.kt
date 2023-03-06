package command.cmd

import LeaderboardObserver
import command.SlashCommand
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.SelectMenu
import org.javacord.api.entity.message.component.SelectMenuOption
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption

class WatchLeaderboardCommand : SlashCommand("watch", "Manage leaderboard watches.") {
    private val leaderboardObservers = mutableSetOf<LeaderboardObserver>()

    private fun oberserversInServer(server: Server)  = leaderboardObservers.filter { it.server == server }

    override fun handle(interaction: SlashCommandInteraction) {
        interaction.createImmediateResponder()
            .setContent("fetching...")
            .setFlags(MessageFlag.EPHEMERAL)
            .respond().thenAccept { responseUpdater ->
                if (!interaction.server.isPresent) {
                    responseUpdater
                        .setErrorMsg("Not allowed in DMs")
                        .update()
                }
                interaction.getOptionByName("list").ifPresent {
                    val embed = EmbedBuilder()
                        .setTitle("Active leaderboard watches: ")
                        .setFooter("Fetching from: ${LeaderboardObserver.LEADERBOARD_ENDPOINT}")
                        .setTimestampToNow()
                        .setDescription(oberserversInServer(interaction.server.get()).joinToString("\n") { "- $it" })
                    responseUpdater
                        .setContent(" ")
                        .addEmbed(embed)
                        .update()
                }
                interaction.getOptionByName("add").ifPresent {
                    val name = interaction.getArgumentStringValueByName("name").get()
                    val taskId = interaction.getArgumentLongValueByName("task").get()
                    val bucketId = interaction.getArgumentLongValueByName("bucket").get()
                    val announceThreshold = interaction.getArgumentLongValueByName("threshold").get()
                    val channel = interaction.getArgumentChannelValueByName("channel").get().asServerTextChannel().get()
                    val newObserver = LeaderboardObserver(taskId, bucketId, name, announceThreshold, channel)

                    if (!channel.canYouWrite()) {
                        responseUpdater
                            .setErrorMsg("Error: I cannot send messages in ${channel.mentionTag}.")
                            .update()
                        return@ifPresent
                    }

                    newObserver.startTimer()
                    leaderboardObservers.add(newObserver)
                    responseUpdater
                        .setSuccessMsg("Added watch for $newObserver")
                        .update()
                    interaction.api.updateActivity(ActivityType.WATCHING, "leaderboards")
                }
                interaction.getOptionByName("delete").ifPresent {
                    if (oberserversInServer(interaction.server.get()).isEmpty()) {
                        responseUpdater
                            .setErrorMsg("No watches found")
                            .update()
                        return@ifPresent
                    }
                    val actions = ActionRow.of(
                        SelectMenu.createStringMenu("remove-watch", oberserversInServer(interaction.server.get()).map { SelectMenuOption.create(it.toString(), it.hashCode().toString()) })
                    )
                    interaction.api.addMessageComponentCreateListener { mcce ->
                        mcce.messageComponentInteraction.asSelectMenuInteraction().ifPresent { smi ->
                            mcce.interaction.createImmediateResponder().respond()
                            if (smi.customId == "remove-watch") {
                                val item = leaderboardObservers.find { it.hashCode().toString() == smi.chosenOptions[0].value }
                                if (item != null) {
                                    leaderboardObservers.remove(item)
                                    responseUpdater
                                        .removeAllComponents()
                                        .setSuccessMsg("Successfully removed $item!")
                                        .update()
                                }
                            }
                        }
                    }
                    responseUpdater
                        .setContent("Which would you like to delete?")
                        .addComponents(actions)
                        .update()
                }
            }
    }

    override val options = listOf(
        SlashCommandOption.createSubcommand("list", "List all active leaderboard watches."),
        SlashCommandOption.createSubcommand("delete", "Delete an active leaderboard watch."),
        SlashCommandOption.createSubcommand("add", "Add a new leaderboard watch.", listOf(
            SlashCommandOption.createStringOption("name", "Name of the board.", true),
            SlashCommandOption.createLongOption("task", "Id of the task.", true),
            SlashCommandOption.createLongOption("bucket", "Id of the bucket.", true),
            SlashCommandOption.createLongOption(
                "threshold",
                "Above this rank changes will be announced to the channel.",
                true,
                1,
                100
            ),
            SlashCommandOption.createChannelOption(
                "channel",
                "Channel in which to announce movement at the top of the leaderboard.",
                true, listOf(ChannelType.SERVER_TEXT_CHANNEL)),
        ))
    )
}