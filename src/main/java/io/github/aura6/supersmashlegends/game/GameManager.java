package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.EndState;
import io.github.aura6.supersmashlegends.game.state.GameState;
import io.github.aura6.supersmashlegends.game.state.InGameState;
import io.github.aura6.supersmashlegends.game.state.LobbyState;
import io.github.aura6.supersmashlegends.game.state.PreGameState;
import io.github.aura6.supersmashlegends.game.state.TutorialState;
import io.github.aura6.supersmashlegends.utils.message.Chat;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager {
    private final SuperSmashLegends plugin;

    private final List<GameState> states = new ArrayList<>();
    private int stateIdx = 0;

    private final Map<UUID, InGameProfile> profiles = new HashMap<>();

    private BukkitTask tickTask;
    @Getter private int ticksActive = 0;

    private final Set<Player> spectators = new HashSet<>();

    public GameManager(SuperSmashLegends plugin) {
        this.plugin = plugin;

        states.add(new LobbyState(plugin));
        states.add(new TutorialState(plugin));
        states.add(new PreGameState(plugin));
        states.add(new InGameState(plugin));
        states.add(new EndState(plugin));
    }

    public GameState getState() {
        return states.get(stateIdx);
    }

    public void activateState() {
        getState().start();
        Bukkit.getPluginManager().registerEvents(getState(), plugin);
    }

    public void endState() {
        getState().end();
        HandlerList.unregisterAll(getState());
    }

    public void advanceState() {
        endState();
        stateIdx = (stateIdx + 1) % states.size();
        activateState();
    }

    public void skipToState(GameState state) {

        if (getState().isSame(state)) {
            Chat.GAME.broadcast("&7Cannot skip to the current state.");
            return;
        }

        endState();

        while (!getState().isSame(state)) {
            stateIdx = (stateIdx + 1) % states.size();
            activateState();

            if (!getState().isSame(state)) {
                endState();
            }
        }
    }

    public Optional<GameState> findState(String name) {
        return states.stream().filter(state -> state.getConfigName().equalsIgnoreCase(name)).findAny();
    }

    public void startTicks() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> ticksActive++, 0, 0);
    }

    public void setupProfile(Player player) {
        profiles.put(player.getUniqueId(), new InGameProfile(plugin.getResources().getConfig().getInt("Game.Lives")));
    }

    public InGameProfile getProfile(Player player) {
        return profiles.get(player.getUniqueId());
    }

    public boolean hasProfile(Player player) {
        return profiles.containsKey(player.getUniqueId());
    }

    public void wipePlayer(Player player) {
        profiles.remove(player.getUniqueId());
    }

    public boolean isPlayerParticipating(Player player) {
        return profiles.containsKey(player.getUniqueId());
    }

    public Set<Player> getParticipators() {
        return profiles.keySet().stream().map(Bukkit::getPlayer).filter(this::isPlayerParticipating).collect(Collectors.toSet());
    }

    public boolean isPlayerAlive(Player player) {
        return isPlayerParticipating(player) && profiles.get(player.getUniqueId()).getLives() > 0;
    }

    public Set<Player> getAlivePlayers() {
        return profiles.keySet().stream().map(Bukkit::getPlayer).filter(this::isPlayerAlive).collect(Collectors.toSet());
    }

    public void addSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(player);
    }

    public void removeSpectator(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        spectators.remove(player);
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public void reset() {
        profiles.clear();
        spectators.clear();
        ticksActive = 0;

        if (tickTask != null) {
            tickTask.cancel();
        }
    }
}
