package command

import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction

abstract class SlashCommand(val name: String, val desc: String) {
    abstract fun handle(interaction: SlashCommandInteraction)

    open fun toSlashCommandBuilder() =
        SlashCommandBuilder()
            .setName(name)
            .setDescription(desc)
}