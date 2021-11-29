package discord.snippets.Embeds.trackInfo;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.EmojiIds;
import utils.interpretations.Interpretation;
import utils.interpretations.soundcloud.SoundCloudPlaylistInterpretation;
import utils.interpretations.spotify.SpotifyPlaylistInterpretation;
import utils.interpretations.youtube.YoutubePlaylistInterpretation;

import java.awt.*;

public class PlayPlaylist {
    public static String formatSource(Interpretation link) {
        if (link instanceof YoutubePlaylistInterpretation) {
            return String.format(
                    "%s %s",
                    EmojiIds.youtubeEmoji,
                    link.getUrl()
            );
        } else if (link instanceof SpotifyPlaylistInterpretation) {
            return String.format(
                    "%s %s",
                    EmojiIds.spotifyEmoji,
                    link.getUrl()
            );
        } else if (link instanceof SoundCloudPlaylistInterpretation) {
            return String.format(
                    "%s %s",
                    EmojiIds.soundcloudEmoji,
                    link.getUrl()
            );
        } else {
            return String.format(
                    "%s -",
                    EmojiIds.coolMusicIcon
            );
        }
    }

    public static MessageEmbed embed(AudioPlaylist playlist, Member requester, boolean nowPlaying, Interpretation link) {
        String title;
        if (nowPlaying) {
            title = "Playing";
        } else {
            title = "Queued";
        }
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(
                        String.format(
                            "[%s](%s)",
                            playlist.getName(),
                            link.getUrl()
                        )
                )
                .setColor(Color.ORANGE)
                .setThumbnail(link.getThumbnailUrl())
                .addField("Creator", link.getCreator(), true)
                .addField(formatSource(link), "", true)
                .addField("Tracks", String.valueOf(playlist.getTracks().size()), true)
                .setFooter(
                        String.format(
                                "by %s",
                                requester.getNickname()
                        ),
                        requester.getUser().getAvatarUrl()
                )
                .build();
    }
}
