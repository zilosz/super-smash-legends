package io.github.zilosz.newsmashplugin.game;

import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import io.github.zilosz.newsmashplugin.game.team.TeamManager;
import lombok.Getter;

public class Game {
    @Getter private final GameStateManager gameStateManager;
    @Getter private final TeamManager teamManager;

    public Game(NewSmashPlugin plugin) {
        gameStateManager = new GameStateManager(plugin);
        teamManager = new TeamManager(plugin);
    }
}
