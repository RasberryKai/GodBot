package commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.github.cdimascio.dotenv.Dotenv;
import ktSnippets.ErrorsKt;
import ktSnippets.TrackInfoKt;
import lavaplayerHandlers.AudioResultHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import playableInfo.PlayableInfo;
import playableInfo.PlaylistPlayableInfo;
import singeltons.AudioPlayerManagerWrapper;
import singeltons.JDAManager;
import snippets.ErrorMessages;
import utils.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Play implements Command {
    // TODO Emphasize EventExtender.interpretations store interpretations in there

    public static void playPlaylist(
            AudioPlayerExtender audioPlayer,
            List<String> videoIds,
            Member member,
            boolean shuffle
    ) {
        AudioPlayerManager audioPlayerManager = AudioPlayerManagerWrapper
                .getInstance()
                .getManager();

        if (!shuffle) {
            for (String videoId : videoIds) {
                AudioResultHandler audioResultHandler = new AudioResultHandler(
                        audioPlayer,
                        member
                );

                audioPlayerManager.loadItem(
                        String.format(
                                "https://www.youtube.com/watch?v=%s",
                                videoId
                        ),
                        audioResultHandler
                );

                while (audioResultHandler.actionType == 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException ignore) {}
                }
            }
        } else {
            for (String videoId : videoIds) {
                audioPlayerManager.loadItem(
                        String.format(
                                "https://www.youtube.com/watch?v=%s",
                                videoId
                        ),
                        new AudioResultHandler(audioPlayer, member)
                );
            }
        }
    }

    private static String checkParameters(EventExtender event)
            throws CheckFailedException {
        OptionMapping url = event.event.getOption("url");
        if (url == null) {
            throw new CheckFailedException();
        }

        return url.getAsString();
    }

    private static boolean getShuffle(EventExtender event) {
        OptionMapping shuffle = event.event.getOption("shuffle");
        return shuffle != null && shuffle.getAsBoolean();
    }

    public static Future<PlayableInfo> startInfoGathering(String url) {
        return Executors.newCachedThreadPool().submit(() -> DataGatherer.gatherPlayableInfo(url));
    }

    public static AudioResultHandler playVideo(
            AudioPlayerExtender audioPlayer,
            String url,
            Member member
    ) {
        AudioResultHandler audioResultHandler = new AudioResultHandler(
                audioPlayer,
                member
        );

        AudioPlayerManagerWrapper
                .getInstance()
                .getManager()
                .loadItem(
                        url,
                        audioResultHandler
                );

        return audioResultHandler;
    }

    private static void processPlaylist(
            AudioPlayerExtender player,
            Member member,
            boolean shuffle,
            InteractionHook interactionHook,
            Future<PlayableInfo> playableFuture,
            JDA bot,
            Guild guild,
            VoiceChannel voiceChannel
    ) {
        PlaylistPlayableInfo playlistInformation;
        try{
            playlistInformation = (PlaylistPlayableInfo) playableFuture.get();
        } catch (InterruptedException e) {
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(
                                    ErrorMessages.INFO_GATHERING_PLAYLIST_FAILED
                            )
                    ).queue();
            return;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InvalidURLException) {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(
                                        ErrorMessages.PLAY_INVALID_URL
                                )
                        ).queue();
                return;
            } else if (e.getCause() instanceof PlatformNotFoundException) {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(ErrorMessages.PLATFORM_NOT_FOUND)
                        ).queue();
                return;
            }
            e.printStackTrace();
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(
                                    ErrorMessages.INFO_GATHERING_PLAYLIST_FAILED
                            )
                    ).queue();
            return;
        }

        int positionInQueue = player.getQueue().size() + 1;
        AudioPlayerExtender audioPlayer = AudioPlayerManagerWrapper
                .getInstance()
                .getPlayer(
                        bot,
                        guild.getId(),
                        voiceChannel
                );

        Executors.newCachedThreadPool().submit(() -> playPlaylist(
                audioPlayer,
                playlistInformation.getVideoIds(),
                member,
                shuffle
        ));

        interactionHook
                .sendMessageEmbeds(
                        TrackInfoKt.playPlaylist(
                                member,
                                playlistInformation,
                                positionInQueue,
                                positionInQueue + playlistInformation.getVideoIds().size()
                        )
                ).queue();
    }

    private static void processVideo(
            String url,
            AudioPlayerExtender player,
            Member member,
            InteractionHook interactionHook,
            Future<PlayableInfo> playableFuture
    ) {
        AudioResultHandler audioResultHandler = playVideo(
                player,
                url,
                member
        );
        System.out.println("playing video");

        while (audioResultHandler.actionType == 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ignore) {}
        }

        switch (audioResultHandler.actionType) {
            case 10 -> {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(ErrorMessages.GENERAL_ERROR)
                        ).queue();
                return;
            }
            case 3 -> {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(ErrorMessages.VIDEO_NOT_FOUND)
                        ).queue();
                return;
            }
            case 4 -> {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(ErrorMessages.LOADING_FAILED)
                        ).queue();
                return;
            }
        }

        System.out.println("gathering playable Future");
        PlayableInfo playableInfo;
        try {
            playableInfo = playableFuture.get();
        } catch (InterruptedException e) {
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(ErrorMessages.INFO_GATHERING_SONG_FAILED)
                    ).queue();
            return;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InvalidURLException) {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(
                                        ErrorMessages.INVALID_URL
                                )
                        ).queue();
                return;
            } else if (e.getCause() instanceof PlatformNotFoundException) {
                interactionHook
                        .sendMessageEmbeds(
                                ErrorsKt.standardError(ErrorMessages.PLATFORM_NOT_FOUND)
                        ).queue();
                return;
            }
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(
                                    ErrorMessages.INFO_GATHERING_SONG_FAILED
                            )
                    ).queue();
            return;
        }

        MessageEmbed embed = TrackInfoKt.playVideo(
                member,
                audioResultHandler.position == 0,
                playableInfo,
                audioResultHandler.position,
                player.getQueue().size() + 1
        );
        System.out.println("About to play");
        interactionHook.sendMessageEmbeds(embed).queue();
    }

    public static void trigger(@NotNull SlashCommandEvent scEvent) {
        EventExtender event = new EventExtender(scEvent);
        String url;
        try {
            url = checkParameters(event);
        } catch (CheckFailedException e) {
            event
                .replyEphemeral(
                        ErrorsKt.standardError("No URL provided")
                );
            return;
        }

        Dotenv dotenv = Dotenv.load();
        Guild guild = scEvent.getGuild();
        Member member = scEvent.getMember();
        String applicationId = dotenv.get("APPLICATIONID");

        VoiceChannel voiceChannel;

        try {
            voiceChannel = Checks.slashCommandCheck(
                    applicationId,
                    member,
                    guild
            );
        } catch (VoiceCheckFailedException e) {
            event.replyEphemeral(
                    ErrorsKt.standardError(
                            ErrorMessages.NOT_CONNECTED_TO_VC
                    )
                    );
            return;
        } catch (CheckFailedException e) {
            event.replyEphemeral(
                    ErrorsKt.standardError(
                            ErrorMessages.GENERAL_ERROR
                    )
            );
            return;
        }

        try {
            checkParameters(event);
        } catch (CheckFailedException e) {
            return;
        }

        if (Checks.linkIsValid(url)) {
            event
                    .replyEphemeral(
                            ErrorsKt.standardError(ErrorMessages.PLAY_INVALID_URL)
                    );
            return;
        }

        JDA bot = JDAManager.getInstance().getJDA(applicationId);

        AudioPlayerExtender player = AudioPlayerManagerWrapper
                .getInstance()
                .getPlayer(
                        bot,
                        guild.getId(),
                        voiceChannel
                );

        if (!player.getVoiceChannel().getId().equals(voiceChannel.getId())) {
            event.replyEphemeral(
                    ErrorsKt.standardError(
                            ErrorMessages.NO_PLAYER_IN_VC
                    )
            );
            return;
        }

        InteractionHook interactionHook = scEvent.getHook();
        scEvent.deferReply().queue();

        Future<PlayableInfo> infoGatheringFuture = startInfoGathering(url);

        boolean isVideo;
        try {
            isVideo = LinkHelper.isVideo(url);
        } catch (InvalidURLException e) {
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(ErrorMessages.PLAY_INVALID_URL)
                    ).queue();
            return;
        } catch (PlatformNotFoundException e) {
            interactionHook
                    .sendMessageEmbeds(
                            ErrorsKt.standardError(ErrorMessages.PLATFORM_NOT_FOUND)
                    ).queue();
            return;
        }

        if (!isVideo) {
            processPlaylist(
                    player,
                    member,
                    getShuffle(event),
                    interactionHook,
                    infoGatheringFuture,
                    bot,
                    guild,
                    voiceChannel
            );
        } else {
            processVideo(
                    url,
                    player,
                    member,
                    interactionHook,
                    infoGatheringFuture
            );
        }
    }
}
