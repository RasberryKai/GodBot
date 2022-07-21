package ktCommands

import commands.Command
import constants.loadingFailed
import constants.notReceivedParameter
import ktUtils.getPlayerWithQueue
import objects.EventFacade
import objects.SlashCommandPayload
import singeltons.JDAManager

fun skipTo(event: EventFacade, payload: SlashCommandPayload) {
    val position: Long? = event.getOption("position")?.asLong
    if (position == null) {
        event.error(notReceivedParameter)
        return
    }

    val player = getPlayerWithQueue(
        JDAManager.getInstance().getJDA(Command.applicationId),
        payload.guild.id,
        payload.voiceChannel.id,
        event
    ) ?: return

    try {
        player.skipTo((position - 1).toInt())
    } catch (e: IndexOutOfBoundsException) {
        event.error("The Queue is ${player.queue.size} big and the position is $position")
        return
    }

    if (player.currentTrack == null) {
        event.error(loadingFailed)
        return
    }

    event.replyLink("Skipped to `$position`, now playing " +
            "[${player.currentTrack!!.songInfo.title}](${player.currentTrack!!.songInfo.uri})")
}