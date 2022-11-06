package io.github.aura6.supersmashlegends.team;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TeamManager {
    private final SuperSmashLegends plugin;
    private final List<Team> teams = new ArrayList<>();

    private final List<TeamData> teamColors = Arrays.asList(
            new TeamData("Yellow", "&e", 4),
            new TeamData("Blue", "&b", 3),
            new TeamData("Red", "&c", 14),
            new TeamData("Lime", "&a", 5),
            new TeamData("Pink", "&d", 2),
            new TeamData("Purple", "&5", 10),
            new TeamData("Green", "&2", 13),
            new TeamData("Turquoise", "&3", 9),
            new TeamData("Orange", "&6", 1),
            new TeamData("Black", "&0", 15),
            new TeamData("White", "&f", 0)
    );

    public TeamManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
        teamColors.subList(0, getTeamCount()).forEach(colors -> teams.add(new Team(colors)));
    }

    private Section getTeamConfig() {
        return plugin.getResources().getConfig().getSection("Game.Team");
    }

    public int getTeamSize() {
        return getTeamConfig().getInt("Size");
    }

    public int getTeamCount() {
        return Math.min(getTeamConfig().getInt("Count"), teamColors.size());
    }

    public int getPlayerCap() {
        return getTeamCount() * getTeamSize();
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public Optional<Team> getChosenTeam(Player player) {
        return teams.stream().filter(team -> team.hasPlayer(player)).findAny();
    }

    public boolean canPlayerJoinTeam(Player player, Team team) {
        return !team.hasPlayer(player) && team.getSize() < getTeamSize();
    }

    public void assignPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> teams.stream()
                .filter(team -> canPlayerJoinTeam(player, team))
                .findFirst().ifPresent(team -> team.addPlayer(player)));
    }
}
