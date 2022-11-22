package io.github.aura6.supersmashlegends.team;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamManager {

    private static final List<TeamData> TEAM_COLORS = Arrays.asList(
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

    private final SuperSmashLegends plugin;
    private final List<Team> teams = new ArrayList<>();
    private final Map<UUID, Team> entityTeams = new HashMap<>();

    public TeamManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
        grabTeams();
    }

    private void grabTeams() {
        TEAM_COLORS.subList(0, getTeamCount()).forEach(colors -> teams.add(new Team(plugin, colors)));
    }

    private Section getTeamConfig() {
        return plugin.getResources().getConfig().getSection("Game.Team");
    }

    public int getTeamSize() {
        return getTeamConfig().getInt("Size");
    }

    public int getTeamCount() {
        return Math.min(getTeamConfig().getInt("Count"), TEAM_COLORS.size());
    }

    public int getPlayerCap() {
        return getTeamCount() * getTeamSize();
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public Team getPlayerTeam(Player player) {
        return entityTeams.get(player.getUniqueId());
    }

    public Optional<Team> findChosenTeam(Player player) {
        return teams.stream().filter(team -> team.hasPlayer(player)).findAny();
    }

    public void assignPlayer(Player player) {
        teams.stream().filter(team -> team.hasPlayer(player)).findAny().ifPresentOrElse(
                chosen -> entityTeams.put(player.getUniqueId(), chosen), () -> {
                    teams.stream().filter(team -> team.canJoin(player)).findFirst().ifPresent(team -> {
                        team.addPlayer(player);
                        entityTeams.put(player.getUniqueId(), team);
                    });
                });
    }

    public void removeEmptyTeams() {
        teams.removeIf(Team::isEmpty);
    }

    public List<Team> getAliveTeams() {
        return teams.stream().filter(Team::isAlive).collect(Collectors.toList());
    }

    public boolean isGameTieOrWin() {
        return teams.stream().filter(Team::isAlive).count() <= 1;
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(entityTeams.remove(player.getUniqueId())).ifPresent(team -> {
            team.removePlayer(player);
            if (team.isEmpty()) {
                teams.remove(team);
            }
        });
    }

    public Optional<Team> findEntityTeam(LivingEntity entity) {
        return Optional.ofNullable(entityTeams.getOrDefault(entity.getUniqueId(), null));
    }

    public void reset() {
        teams.clear();
        entityTeams.clear();
        grabTeams();
    }
}
