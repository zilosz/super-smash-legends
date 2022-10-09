package io.github.zilosz.newsmashplugin.utils.message;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import org.bukkit.entity.Player;

public class Chatter {
    private final Section chatSection;

    public Chatter(NewSmashPlugin plugin) {
        chatSection = plugin.getMessageConfig().getSection("chat");
    }

    public void sendMessage(Player player, String chatType, String messagePath, Replacer... replacers) {
        Section section = chatSection.getSection(chatType);
        player.sendMessage(MessageUtils.parse(section.getString("prefix") + section.getString(messagePath), replacers));
    }
}
