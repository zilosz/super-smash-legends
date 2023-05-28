package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.database.Database;
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

    public boolean isPlayerParticipating(Player player) {
        return player != null && profiles.containsKey(player.getUniqueId()) && !isSpectator(player);
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

    public void uploadPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        InGameProfile profile = profiles.get(uuid);
        Database db = plugin.getDb();

        db.setIfEnabled(uuid, "kills", db.getOrDefault(uuid, "kills", 0, 0) + profile.getKills());
        db.setIfEnabled(uuid, "deaths", db.getOrDefault(uuid, "deaths", 0, 0) + profile.getDeaths());
        db.setIfEnabled(uuid, "damageTaken", db.getOrDefault(uuid, "damageTaken", 0.0, 0.0) + profile.getDamageTaken());
        db.setIfEnabled(uuid, "damageDealt", db.getOrDefault(uuid, "damageDealt", 0.0, 0.0) + profile.getDamageDealt());
    }

    public void uploadPlayerStatsMidGame(Player player) {
        uploadPlayerStats(player);

        int before = plugin.getDb().getOrDefault(player.getUniqueId(), "losses", 0, 0);
        plugin.getDb().setIfEnabled(player.getUniqueId(), "losses", before + 1);
    }

    public void uploadPlayerStatsAtEnd(Player player) {
        uploadPlayerStats(player);

        Database db = this.plugin.getDb();
        UUID uuid = player.getUniqueId();

        if (getProfile(player).isWinner()) {

            if (this.getParticipators().size() > 1) {
                db.setIfEnabled(uuid, "wins", db.getOrDefault(uuid, "wins", 0, 0) + 1);
            }

        } else {
            db.setIfEnabled(uuid, "losses", db.getOrDefault(uuid, "losses", 0, 0) + 1);
        }
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
