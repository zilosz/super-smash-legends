package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.game.state.GameStateType;
import com.github.zilosz.ssl.kit.Kit;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameManager {
    private final List<GameStateType> states = new ArrayList<>();

    private final Map<Player, InGameProfile> profiles = new HashMap<>();

    private final Set<Player> willSpectate = new HashSet<>();
    private final Set<Player> spectators = new HashSet<>();

    private int stateIdx = 0;
    @Getter private GameState state;

    private BukkitTask tickTask;
    @Getter private int ticksActive = 0;

    public GameManager() {
        this.states.add(GameStateType.LOBBY);
        this.states.add(GameStateType.TUTORIAL);
        this.states.add(GameStateType.PREGAME);
        this.states.add(GameStateType.IN_GAME);
        this.states.add(GameStateType.END);

        this.updateState();
    }

    private void updateState() {
        GameStateType type = this.states.get(this.stateIdx);
        this.state = type.get();
        this.state.setType(type);
    }

    public void addFutureSpectator(Player player) {
        this.willSpectate.add(player);
    }

    public void removeFutureSpectator(Player player) {
        this.willSpectate.remove(player);
    }

    public boolean willSpectate(Player player) {
        return this.willSpectate.contains(player);
    }

    public void skipToState(GameStateType type) {
        while (this.state.getType() != type) {
            this.advanceState();
        }
    }

    public void advanceState() {
        this.endState();
        this.stateIdx = (this.stateIdx + 1) % this.states.size();
        this.updateState();
        this.activateState();
    }

    public void endState() {
        this.state.end();
        HandlerList.unregisterAll(this.state);
    }

    public void activateState() {
        this.state.start();
        Bukkit.getPluginManager().registerEvents(this.state, SSL.getInstance());
    }

    public void startTicks() {
        this.tickTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> this.ticksActive++, 0, 0);
    }

    public void setupProfile(Player player) {
        int lives = SSL.getInstance().getResources().getConfig().getInt("Game.Lives");
        Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
        this.profiles.put(player, new InGameProfile(lives, kit));
    }

    public Set<Player> getAlivePlayers() {
        return this.profiles.keySet().stream()
                .filter(Player::isOnline)
                .filter(this::isPlayerAlive)
                .collect(Collectors.toSet());
    }

    public boolean isPlayerAlive(Player player) {
        return !this.isSpectator(player) && this.getProfile(player).getLives() > 0;
    }

    public boolean isSpectator(Player player) {
        return this.spectators.contains(player);
    }

    public InGameProfile getProfile(Player player) {
        return this.profiles.get(player);
    }

    public void reset() {
        new HashSet<>(this.spectators).forEach(this::removeSpectator);
        this.profiles.clear();

        this.ticksActive = 0;

        if (this.tickTask != null) {
            this.tickTask.cancel();
        }
    }

    public void removeSpectator(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        this.spectators.remove(player);
    }

    public void addSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        this.spectators.add(player);
    }
}
