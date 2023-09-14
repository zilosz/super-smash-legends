package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.effects.ColorType;
import com.github.zilosz.ssl.util.message.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Team {
    @Getter private final ColorType colorType;
    @Getter private final int playerCap;
    private final Set<Player> players = new HashSet<>();
    private final Set<LivingEntity> entities = new HashSet<>();
    @Getter @Setter private int lifespan;

    public Team(ColorType colorType, int playerCap) {
        this.colorType = colorType;
        this.playerCap = playerCap;
    }

    public String getName() {
        return MessageUtils.color(this.colorType.getChatSymbol() + this.colorType.getName());
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(this.players);
    }

    public boolean isAlive() {
        return this.players.stream().allMatch(player -> SSL.getInstance().getGameManager().isPlayerAlive(player));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    void addEntity(LivingEntity entity) {
        if (entity instanceof Player) {
            this.players.add((Player) entity);
        } else {
            this.entities.add(entity);
        }
    }

    void removeEntity(LivingEntity entity) {
        if (entity instanceof Player) {
            this.players.remove(entity);
        } else {
            this.entities.remove(entity);
        }
    }

    public boolean hasEntity(LivingEntity entity) {
        return this.entities.contains(entity) || entity instanceof Player && this.players.contains((Player) entity);
    }
}
