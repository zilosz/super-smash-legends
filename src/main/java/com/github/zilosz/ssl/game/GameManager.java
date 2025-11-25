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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GameManager {
  private static final GameStateType[] STATES = {
      GameStateType.LOBBY,
      GameStateType.TUTORIAL,
      GameStateType.PREGAME,
      GameStateType.IN_GAME,
      GameStateType.END
  };

  private final Map<Player, InGameProfile> profiles = new HashMap<>();

  private final Collection<Player> willSpectate = new HashSet<>();
  private final Set<Player> spectators = new HashSet<>();

  private int stateIdx;
  @Getter private GameState state;

  private BukkitTask tickTask;
  @Getter private int ticksActive;

  public GameManager() {
    updateState();
  }

  private void updateState() {
    GameStateType stateType = STATES[stateIdx];
    state = stateType.get();
    state.setType(stateType);
  }

  public void addFutureSpectator(Player player) {
    willSpectate.add(player);
  }

  public void removeFutureSpectator(Player player) {
    willSpectate.remove(player);
  }

  public boolean willSpectate(Player player) {
    return willSpectate.contains(player);
  }

  public void skipToState(GameStateType type) {
    while (state.getType() != type) {
      advanceState();
    }
  }

  public void advanceState() {
    state.end();
    HandlerList.unregisterAll(state);

    stateIdx = (stateIdx + 1) % STATES.length;

    updateState();
    activateState();
  }

  public void activateState() {
    state.start();
    Bukkit.getPluginManager().registerEvents(state, SSL.getInstance());
  }

  public void startTicks() {
    tickTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> ticksActive++, 0, 0);
  }

  public void setupProfile(Player player) {
    int lives = SSL.getInstance().getResources().getConfig().getInt("Game.Lives");
    Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
    profiles.put(player, new InGameProfile(kit, lives));
  }

  public Set<Player> getAlivePlayers() {
    return profiles.keySet().stream().filter(this::isPlayerAlive).collect(Collectors.toSet());
  }

  public boolean isPlayerAlive(Player player) {
    return Optional.ofNullable(getProfile(player)).map(prof -> prof.getLives() > 0).orElse(false);
  }

  public InGameProfile getProfile(Player player) {
    return profiles.get(player);
  }

  public boolean isSpectator(Player player) {
    return spectators.contains(player);
  }

  public Set<Player> getSpectators() {
    return Collections.unmodifiableSet(spectators);
  }

  public void reset() {
    new HashSet<>(spectators).forEach(this::removeSpectator);
    profiles.clear();

    ticksActive = 0;

    if (tickTask != null) {
      tickTask.cancel();
    }
  }

  public void removeSpectator(Player player) {
    spectators.remove(player);
    player.setAllowFlight(false);
    player.setFlySpeed(0.1f);
    Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(player));
  }

  public void addSpectator(Player player) {
    spectators.add(player);
    player.setGameMode(GameMode.SURVIVAL);
    player.setAllowFlight(true);
    player.setFlySpeed(0.2f);
    Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(player));
  }
}
