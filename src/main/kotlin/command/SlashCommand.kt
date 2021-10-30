package command

import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandPermissions

abstract class SlashCommand(val name: String, val desc: String) {
    var specialPermissions = mutableListOf<SlashCommandPermissions>()

    abstract fun handle(interaction: SlashCommandInteraction)

    open fun toSlashCommandBuilder() =
        SlashCommandBuilder()
            .setName(name)
            .setDescription(desc)
}