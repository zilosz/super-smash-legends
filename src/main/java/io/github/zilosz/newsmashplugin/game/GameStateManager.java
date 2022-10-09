package io.github.zilosz.newsmashplugin.game;

import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import io.github.zilosz.newsmashplugin.game.state.GameState;
import io.github.zilosz.newsmashplugin.game.state.LobbyState;
import lombok.Getter;
import org.bukkit.event.HandlerList;

public class GameStateManager {
    @Getter private GameState gameState;

    public GameStateManager(NewSmashPlugin plugin) {
        setState(new LobbyState(plugin));
    }

    private void setState(GameState gameState) {
        this.gameState = gameState;
        gameState.register();
    }

    public void changeState(GameState gameState) {
        HandlerList.unregisterAll(this.gameState);
        setState(gameState);
    }
}
