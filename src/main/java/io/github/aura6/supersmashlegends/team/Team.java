package io.github.aura6.supersmashlegends.team;

import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Team {
    private final TeamData data;
    private final Set<UUID> players = new HashSet<>();

    public Team(TeamData data) {
        this.data = data;
    }

    public String getName() {
        return MessageUtils.color(data.getTextColor() + data.getName());
    }

    public ItemStack getItemStack() {
        return new ItemStack(Material.WOOL, 1, (byte) data.getWoolData());
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public int getSize() {
        return players.size();
    }

    public List<Player> getPlayers() {
        return players.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
    }
}
