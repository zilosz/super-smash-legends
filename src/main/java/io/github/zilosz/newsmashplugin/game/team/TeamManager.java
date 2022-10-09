package io.github.zilosz.newsmashplugin.game.team;

import io.github.zilosz.newsmashplugin.NewSmashPlugin;

public class TeamManager {
    private final int teamSize;
    private final int teamCount;

    public TeamManager(NewSmashPlugin plugin) {
        teamSize = plugin.getConfig().getInt("teamSize");
        teamCount = plugin.getConfig().getInt("teamCount");
    }

    public int getMaxPlayers() {
        return teamSize * teamCount;
    }
}
