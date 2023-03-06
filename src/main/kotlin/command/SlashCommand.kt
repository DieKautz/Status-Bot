package command

import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import java.awt.Color

abstract class SlashCommand(val name: String, private val desc: String) {
    open val defaultDisabled = true
    open val options: List<SlashCommandOption> = listOf()

    protected fun InteractionOriginalResponseUpdater.setSuccessMsg(content: String): InteractionOriginalResponseUpdater =
        this.setContent(" ").addEmbed(simpleMsgEmbed(content, Color(67, 181, 129)))

    protected fun InteractionOriginalResponseUpdater.setErrorMsg(content: String): InteractionOriginalResponseUpdater =
        this.setContent(" ").addEmbed(simpleMsgEmbed(content, Color(240, 71, 71)))

    private fun simpleMsgEmbed(msg: String, color: Color) = EmbedBuilder()
        .setColor(color)
        .setDescription(msg)

    abstract fun handle(interaction: SlashCommandInteraction)

    fun toSlashCommandBuilder(): SlashCommandBuilder {
        val slashCommandBuilder = SlashCommandBuilder()
            .setName(name)
            .setDescription(desc)
        if (defaultDisabled) {
            slashCommandBuilder.setDefaultDisabled()
        }
        if (options.isNotEmpty()) {
            slashCommandBuilder.setOptions(options)
        }
        return slashCommandBuilder
    }
}