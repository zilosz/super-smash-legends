package io.github.aura6.supersmashlegends.team;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Team {
    private final SuperSmashLegends plugin;
    private final TeamData data;
    private final List<UUID> players = new ArrayList<>();
    private final Set<LivingEntity> entities = new HashSet<>();
    @Getter @Setter private int lifespan;

    public Team(SuperSmashLegends plugin, TeamData data) {
        this.plugin = plugin;
        this.data = data;
    }

    public String getColor() {
        return data.getTextColor();
    }

    public String getName() {
        return MessageUtils.color(getColor() + data.getName());
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

    public void addEntity(LivingEntity entity) {
        entities.add(entity);
    }

    public boolean hasEntity(LivingEntity entity) {
        return entities.contains(entity) || players.contains(entity.getUniqueId());
    }

    public void removeEntity(LivingEntity entity) {
        entities.remove(entity);
    }

    public int getSize() {
        return players.size();
    }

    public boolean isEmpty() {
        return players.size() == 0;
    }

    public List<Player> getPlayers() {
        return players.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
    }

    public boolean isAlive() {
        return getPlayers().stream().allMatch(player -> plugin.getGameManager().isPlayerAlive(player));
    }

    public boolean canJoin(Player player) {
        return !hasPlayer(player) && players.size() < plugin.getTeamManager().getTeamSize();
    }
}
