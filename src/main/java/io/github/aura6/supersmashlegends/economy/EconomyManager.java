package io.github.aura6.supersmashlegends.economy;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final SuperSmashLegends plugin;
    private final Map<UUID, Integer> jewels = new HashMap<>();

    public EconomyManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    private Section getConfig() {
        return plugin.getResources().getConfig().getSection("Economy");
    }

    public void setupUser(Player player) {
        int offline = getConfig().getInt("OfflineJewels");
        int starting = getConfig().getInt("StartingJewels");
        jewels.put(player.getUniqueId(), plugin.getDb().getOrDefault(player.getUniqueId(), "jewels", starting, offline));
    }

    public void uploadUser(Player player) {
        plugin.getDb().setIfEnabled(player.getUniqueId(), "jewels", jewels.get(player.getUniqueId()));
    }

    public int getJewels(Player player) {
        return jewels.get(player.getUniqueId());
    }

    public void setJewels(Player player, int amount) {
        jewels.put(player.getUniqueId(), amount);
    }

    public void addJewels(Player player, int amount) {
        setJewels(player, getJewels(player) + amount);
        Chat.ECONOMY.send(player, MessageUtils.color(String.format("&7Added &f%d &7jewels to your account.", amount)));
    }

    public void subtractJewels(Player player, int amount) {
        setJewels(player, Math.max(getJewels(player) - amount, 0));
        Chat.ECONOMY.send(player, MessageUtils.color(String.format("&7Subtracted &f%d &7jewels from your account.", amount)));
    }

    public boolean tryPurchase(Player player, int amount) {
        boolean hasEnough = getJewels(player) >= amount;

        if (hasEnough) {
            subtractJewels(player, amount);

        } else {
            Chat.ECONOMY.send(player, MessageUtils.color("&7You don't have enough jewels!"));
        }

        return hasEnough;
    }
}
