package ktCommands

import io.github.cdimascio.dotenv.Dotenv
import ktSnippets.standardError
import ktUtils.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import playableInfo.PlayableInfo
import singeltons.AudioPlayerManagerWrapper
import singeltons.JDAManager
import singeltons.PlayerVault
import snippets.Colours
import snippets.EmojiIds
import snippets.ErrorMessages
import utils.Checks
import utils.EventExtender

fun remove(event: EventExtender) {
    fun removeCheckParameters(event: SlashCommandEvent): Long {
        val position: OptionMapping = event.getOption("position") ?: throw ArgumentNotFoundException()
        return position.asLong
    }

    val applicationId: String? = Dotenv.load()["APPLICATIONID"]
    val guild: Guild? = event.event.guild

    val position: Int

    try {
        position = removeCheckParameters(event.event).toInt()
    } catch (e: CheckFailedException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NOT_RECEIVED_PARAMETER
            )
        )
        return
    }

    val audioPlayer: AudioPlayerExtender

    try {
        audioPlayer = PlayerVault
            .getInstance()
            .getPlayer(
                JDAManager.getInstance().getJDA(applicationId),
                guild!!.id
            )
    } catch (e: PlayerNotFoundException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NO_PLAYER_IN_VC
            )
        )
        return
    }

    val playableInfo: PlayableInfo

    try {
        playableInfo = audioPlayer.removeTrackAt(position - 1)
    } catch (e: IndexOutOfBoundsException) {
        event.replyEphemeral(
            standardError(
                String.format(
                    "There is no Track at Position `%s` there are only" +
                            " `%s` Tracks in the Queue",
                    position,
                    audioPlayer.queue.size
                )
            )
        )
        return
    }

    event.reply(
        EmbedBuilder()
            .setDescription(
                String.format(
                    "**Removed [%s](%s)**",
                    playableInfo.title,
                    playableInfo.uri
                )
            )
            .setColor(Colours.godbotYellow)
            .build()
    )
}

fun loop(event: EventExtender) {
    fun loopCheckParameter(event: SlashCommandEvent): Boolean {
        val mode: OptionMapping = event.getOption("mode") ?: throw ArgumentNotFoundException()
        return mode.asBoolean
    }

    val applicationId: String? = Dotenv.load()["APPLICATIONID"]
    val guild: Guild? = event.event.guild
    val member: Member? = event.event.member
    val voiceChannel: VoiceChannel
    val mode: Boolean
    try {
        mode = loopCheckParameter(event.event)
    } catch (e: ArgumentNotFoundException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NOT_RECEIVED_PARAMETER
            )
        )
        return
    }

    try {
        voiceChannel = Checks.slashCommandCheck(
            event,
            applicationId,
            member,
            guild
        )
    } catch (e: CheckFailedException) {
        return
    }

    val player: AudioPlayerExtender
    try {
        player = AudioPlayerManagerWrapper
            .getInstance()
            .getPlayer(
                JDAManager.getInstance().getJDA(applicationId),
                guild!!.id,
                voiceChannel
            )
    } catch (e: JDANotFound) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.PLAYER_NOT_FOUND
            )
        )
        return
    } catch (e: GuildNotFoundException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NO_PLAYER_IN_GUILD
            )
        )
        return
    }

    if (player.voiceChannel.id != voiceChannel.id) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NO_PLAYER_IN_VC
            )
        )
        return
    }

    if (player.loop == mode && player.loop) {
        event.replyEphemeral(standardError("The Player is already in Loop Mode"))
        return
    } else if (player.loop == mode) {
        event.replyEphemeral(standardError("Loop mode is not activated for the Player"))
        return
    }

    player.loop = mode

    event.reply(
        EmbedBuilder()
            .setTitle(
                if (mode) {
                    "${EmojiIds.loop} Loop Mode Enabled"
                } else {
                    "${EmojiIds.noLoop} Loop Mode Disabled"
                }
            )
            .setColor(Colours.godbotYellow)
            .build()
    )
}

fun skipTo(event: EventExtender) {
    fun skipCheckParameter(event: SlashCommandEvent): Long {
        val position: OptionMapping = event.getOption("position") ?: throw ArgumentNotFoundException()
        return position.asLong
    }

    val applicationId: String? = Dotenv.load()["APPLICATIONID"]
    val guild: Guild? = event.event.guild
    val member: Member? = event.event.member
    val voiceChannel: VoiceChannel
    val position: Long
    try {
        position = skipCheckParameter(event.event)
    } catch (e: ArgumentNotFoundException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NOT_RECEIVED_PARAMETER
            )
        )
        return
    }

    try {
        voiceChannel = Checks.slashCommandCheck(
            event,
            applicationId,
            member,
            guild
        )
    } catch (e: CheckFailedException) {
        return
    }

    val player: AudioPlayerExtender
    try {
        player = AudioPlayerManagerWrapper
            .getInstance()
            .getPlayer(
                JDAManager.getInstance().getJDA(applicationId),
                guild!!.id,
                voiceChannel
            )
    } catch (e: JDANotFound) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.PLAYER_NOT_FOUND
            )
        )
        return
    } catch (e: GuildNotFoundException) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NO_PLAYER_IN_GUILD
            )
        )
        return
    }

    if (player.voiceChannel.id != voiceChannel.id) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.NO_PLAYER_IN_VC
            )
        )
        return
    }
    if (player.queue.isEmpty()) {
        event.replyEphemeral(
            standardError(
                ErrorMessages.QUEUE_EMPTY
            )
        )
        return
    }

    try {
        player.skipTo((position - 1).toInt())
    } catch (e: IndexOutOfBoundsException) {
        event.replyEphemeral(
            standardError(
                "The Queue is `${player.queue.size}` big and the position is `$position`"
            )
        )
        return
    }

    event.reply(
        EmbedBuilder()
            .setDescription("Skipped to `$position`, now playing " +
                    "[${player.currentTrack!!.audioTrack.info.title}](${player.currentTrack!!.audioTrack.info.uri})")
            .setColor(Colours.godbotYellow)
            .build()
    )
}
