package ktCommands

import ktSnippets.standardError
import ktSnippets.trackLines
import ktUtils.*
import net.dv8tion.jda.api.EmbedBuilder
import singeltons.JDAManager
import singeltons.PlayerVault
import snippets.Colours
import snippets.ErrorMessages
import utils.DurationCalc
import utils.EventExtender
import java.util.concurrent.TimeUnit

fun seek(event: EventExtender, payload: SlashCommandPayload) {
    fun getSeekPoint(): Long {
        val hours = event.getOption("hours")
        val minutes = event.getOption("minutes")
        val seconds = event.getOption("seconds")
        return TimeUnit.HOURS.toMillis(hours?.asLong ?: 0)+
                TimeUnit.MINUTES.toMillis(minutes?.asLong ?: 0) +
                TimeUnit.SECONDS.toMillis(seconds?.asLong ?: 0)
    }

    val player: AudioPlayerExtender
    try {
        player = PlayerVault
            .getInstance()
            .getPlayer(
                JDAManager.getInstance().getJDA(payload.applicationId),
                payload.guild.id
            )
    } catch (e: PlayerNotFoundException) {
        event.replyEphemeral(standardError(ErrorMessages.NO_PLAYER_IN_VC))
        return
    }

    if (player.voiceChannel.id != payload.voiceChannel.id) {
        event.replyEphemeral(standardError(ErrorMessages.NO_PLAYER_IN_VC))
        return
    }
    if (player.queue.isEmpty()) {
        event.replyEphemeral(standardError(ErrorMessages.QUEUE_EMPTY))
        return
    }

    if (player.currentTrack == null) {
        event.replyEphemeral(
            standardError(ErrorMessages.NO_PLAYING_TRACK)
        )
        return
    }

    val seekPoint: Long = getSeekPoint()
    val duration: Long = player.currentTrack?.songInfo?.duration ?: 0

    if (seekPoint > duration) {
        event.replyEphemeral(
            standardError("I can't skip to ${DurationCalc.longToString(seekPoint)}" +
                    ", because the song is only ${DurationCalc.longToString(duration)} long")
        )
        return
    }

    player.seek(seekPoint)

    event.reply(
        EmbedBuilder()
            .setTitle(player.currentTrack?.songInfo?.title)
            .setThumbnail(player.currentTrack?.songInfo?.thumbnailUri)
            .setDescription(
                trackLines(seekPoint, player.getCurrentSongDuration()) + " "
                        + "**${DurationCalc.longToString(seekPoint)} - ${DurationCalc.longToString(duration)}**"
            )
            .setColor(Colours.godbotHeavenYellow)
            .build()
    )
}