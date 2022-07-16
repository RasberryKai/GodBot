package singeltons;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lavaplayerHandlers.AudioPlayerSendHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import ktUtils.ApplicationNotFoundException;

import java.util.HashMap;
import java.util.List;

public class AudioManagerVault {

    private static final AudioManagerVault managerObj = new AudioManagerVault();

    //                   BotJDA        GuildId  Destination
    private final HashMap<JDA, HashMap<String, AudioManager>> audioManagerStorage = new HashMap<>();

    public void registerJDA(JDA botJDA, List<Guild> guilds) {
        if (!audioManagerStorage.containsKey(botJDA)) {
            audioManagerStorage.put(botJDA, new HashMap<>());
        }
        for (Guild guild : guilds) {
            audioManagerStorage.get(botJDA).put(guild.getId(), guild.getAudioManager());
        }
    }

    public void registerGuild(JDA botJDA, Guild guild) {
        if (!audioManagerStorage.containsKey(botJDA)) {
            audioManagerStorage.put(botJDA, new HashMap<>());
        }
        audioManagerStorage.get(botJDA).put(guild.getId(), guild.getAudioManager());
    }

    public void removeJDA(JDA botJDA) {
        audioManagerStorage.remove(botJDA);
    }

    public void removeGuild(JDA botJDA, String guildId)
            throws ApplicationNotFoundException {
        if (!audioManagerStorage.containsKey(botJDA)) {
            throw new ApplicationNotFoundException();
        }
        audioManagerStorage.get(botJDA).remove(guildId);
    }

    public AudioManager getAudioManager(JDA botJDA, String guildId) {
        if (!audioManagerStorage.containsKey(botJDA)) {
            registerJDA(botJDA, botJDA.getGuilds());
            return getAudioManager(botJDA, guildId);
        }
        if (!audioManagerStorage.get(botJDA).containsKey(guildId)) {
            Guild guild = botJDA.getGuildById(guildId);
            registerGuild(botJDA, guild);
            return getAudioManager(botJDA, guildId);
        }
        return audioManagerStorage.get(botJDA).get(guildId);
    }

    public void checkSendingHandler(JDA botJDA, String guildId, AudioPlayer player) {
        AudioManager audioManager;
        if (!audioManagerStorage.containsKey(botJDA)) {
            registerJDA(botJDA, botJDA.getGuilds());
        }
        if (!audioManagerStorage.get(botJDA).containsKey(guildId)) {
            Guild guild = botJDA.getGuildById(guildId);
            registerGuild(botJDA, guild);
        }
        audioManager = getAudioManager(botJDA, guildId);

        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
    }

    public static AudioManagerVault getInstance() { return managerObj; }
}
