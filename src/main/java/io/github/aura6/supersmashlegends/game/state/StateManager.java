package io.github.aura6.supersmashlegends.game.state;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import lombok.Getter;

public class StateManager {
    private final SuperSmashLegends plugin;
    @Getter private GameState state;

    public StateManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
        state = new LobbyState(plugin);
    }

    public void changeState(GameState state) {
        this.state.end();
        this.state = state;
        state.start();
    }

    public void startGame() {
        boolean tutorial = plugin.getResources().getConfig().getBoolean("Game.Tutorial.Enabled");
        changeState(tutorial ? new TutorialState(plugin) : new PreGameState(plugin));
        Chat.GAME.broadcast("&7The game is starting...");
    }
}
