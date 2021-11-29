package discord.snippets.Embeds.errors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.discord.Emojis;

import java.awt.*;

public class StandardError {
    public static MessageEmbed build(String description) {
        return new EmbedBuilder()
                .setDescription(
                        String.format(
                                "%s %s",
                                Emojis.godbotWarning,
                                description
                        )
                )
                .setColor(Color.RED)
                .build();
    }
}
