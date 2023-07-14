package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.effects.ColorType;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Team {
    @Getter private final ColorType colorType;
    private final Set<Player> players = new HashSet<>();
    private final Set<LivingEntity> entities = new HashSet<>();
    @Getter @Setter private int lifespan;

    public Team(ColorType colorType) {
        this.colorType = colorType;
    }

    public String getName() {
        return MessageUtils.color(this.colorType.getChatSymbol() + this.colorType.getName());
    }

    public ItemStack getItemStack() {
        return new ItemStack(Material.WOOL, 1, this.colorType.getDyeColor().getWoolData());
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public void addEntity(LivingEntity entity) {
        this.entities.add(entity);
    }

    public void removeEntity(LivingEntity entity) {
        this.entities.remove(entity);
    }

    public boolean hasAnyEntity(LivingEntity entity) {
        return this.entities.contains(entity) || entity instanceof Player && this.hasPlayer((Player) entity);
    }

    public boolean hasPlayer(Player player) {
        return this.players.contains(player);
    }

    public boolean hasNoPlayers() {
        return this.players.isEmpty();
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(this.players);
    }

    public List<Player> getSortedPlayers() {
        return this.players.stream().sorted(Comparator.comparing(Player::getName)).collect(Collectors.toList());
    }

    public boolean isAlive() {
        return this.players.stream().allMatch(player -> SSL.getInstance().getGameManager().isPlayerAlive(player));
    }

    public boolean canJoin(Player player) {
        return !this.hasPlayer(player) && this.getPlayerCount() < SSL.getInstance().getTeamManager().getTeamSize();
    }

    public int getPlayerCount() {
        return this.players.size();
    }
}
