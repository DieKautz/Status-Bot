package command

import command.cmd.RefetchCommand
import org.javacord.api.DiscordApi
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.ServerSlashCommandPermissionsBuilder
import org.javacord.api.interaction.SlashCommandPermissionType
import org.javacord.api.interaction.SlashCommandPermissions
import org.javacord.api.listener.interaction.SlashCommandCreateListener

class SlashCommandHandler(private val api: DiscordApi, private val commands: List<SlashCommand>) :
    SlashCommandCreateListener {
    init {
        registerCommands()
        api.addSlashCommandCreateListener(this)
    }

    override fun onSlashCommandCreate(event: SlashCommandCreateEvent?) {
        if (event == null) return
        val interaction = event.slashCommandInteraction
        commands.first { it.name == interaction.commandName }.handle(interaction)
    }

    private fun registerCommands() {
        api.globalSlashCommands.join().forEach { it.deleteGlobal().join() }
        api.bulkOverwriteGlobalSlashCommands(
            commands.map { it.toSlashCommandBuilder() }
        ).join()

        val ownerId = 290464744794750976
        val refetchCmdId = api.globalSlashCommands.join().first { it.name == RefetchCommand().name }.id
        api.servers.forEach { srv ->
            api.batchUpdateSlashCommandPermissions(
                srv, mutableListOf(
                    ServerSlashCommandPermissionsBuilder(
                        refetchCmdId,
                        listOf(SlashCommandPermissions.create(ownerId, SlashCommandPermissionType.USER, true))
                    )
                )
            )
        }
    }
}