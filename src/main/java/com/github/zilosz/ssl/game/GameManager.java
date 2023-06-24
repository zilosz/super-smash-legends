package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.EndState;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.game.state.InGameState;
import com.github.zilosz.ssl.game.state.LobbyState;
import com.github.zilosz.ssl.game.state.PreGameState;
import com.github.zilosz.ssl.game.state.TutorialState;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.message.Chat;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager {
    private final List<GameState> states = new ArrayList<>();
    private int stateIdx = 0;

    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    private final Set<Player> willSpectate = new HashSet<>();
    private final Set<Player> spectators = new HashSet<>();

    private BukkitTask tickTask;
    @Getter private int ticksActive = 0;

    public GameManager() {
        this.states.add(new LobbyState());
        this.states.add(new TutorialState());
        this.states.add(new PreGameState());
        this.states.add(new InGameState());
        this.states.add(new EndState());
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

    public void advanceState() {
        this.endState();
        this.stateIdx = (this.stateIdx + 1) % this.states.size();
        this.activateState();
    }

    public void endState() {
        this.getState().end();
        HandlerList.unregisterAll(this.getState());
    }

    public void activateState() {
        this.getState().start();
        Bukkit.getPluginManager().registerEvents(this.getState(), SSL.getInstance());
    }

    public GameState getState() {
        return this.states.get(this.stateIdx);
    }

    public void skipToState(GameState state) {

        if (this.getState().isSame(state)) {
            Chat.GAME.broadcast("&7Cannot skip to the current state.");
            return;
        }

        this.endState();

        while (!this.getState().isSame(state)) {
            this.stateIdx = (this.stateIdx + 1) % this.states.size();
            this.activateState();

            if (!this.getState().isSame(state)) {
                this.endState();
            }
        }
    }

    public Optional<GameState> findState(String name) {
        return this.states.stream().filter(state -> state.getConfigName().equalsIgnoreCase(name)).findAny();
    }

    public void startTicks() {
        this.tickTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> this.ticksActive++, 0, 0);
    }

    public void setupProfile(Player player) {
        int lives = SSL.getInstance().getResources().getConfig().getInt("Game.Lives");
        Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
        this.profiles.put(player.getUniqueId(), new PlayerProfile(lives, kit));
    }

    public Set<Player> getAlivePlayers() {
        return this.profiles.keySet().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .filter(this::isPlayerAlive)
                .collect(Collectors.toSet());
    }

    public boolean isPlayerAlive(Player player) {
        return !this.isSpectator(player) && this.getProfile(player).getLives() > 0;
    }

    public boolean isSpectator(Player player) {
        return this.spectators.contains(player);
    }

    public PlayerProfile getProfile(Player player) {
        return this.profiles.get(player.getUniqueId());
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
