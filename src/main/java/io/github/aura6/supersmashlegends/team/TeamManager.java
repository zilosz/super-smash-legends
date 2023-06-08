package io.github.aura6.supersmashlegends.team;

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

    private final List<Team> teamList = new ArrayList<>();
    private final Map<UUID, Team> teamsByEntity = new HashMap<>();

    public TeamManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
        this.grabTeams();
    }

    private void grabTeams() {
        TEAM_COLORS.forEach(colors -> this.teamList.add(new Team(this.plugin, colors)));
    }

    public int getTeamSize() {
        return this.plugin.getResources().getConfig().getInt("Game.TeamSize");
    }

    public int getAbsolutePlayerCap() {
        return TEAM_COLORS.size() * this.getTeamSize();
    }

    public List<Team> getTeamList() {
        return Collections.unmodifiableList(teamList);
    }

    public Team getPlayerTeam(Player player) {
        return this.teamsByEntity.get(player.getUniqueId());
    }

    public boolean doesPlayerHaveTeam(Player player) {
        return this.teamsByEntity.containsKey(player.getUniqueId());
    }

    public Optional<Team> findChosenTeam(Player player) {
        return this.teamList.stream().filter(team -> team.hasPlayer(player)).findAny();
    }

    public String getPlayerColor(Player player) {
        if (this.getTeamSize() == 1) {
            return this.plugin.getGameManager().getProfile(player).getKit().getColor();
        }
        return this.getPlayerTeam(player).getColor();
    }

    public void assignPlayer(Player player) {
        boolean assigned = false;

        for (Team team : this.teamList) {

            if (team.hasPlayer(player) || team.canJoin(player)) {
                this.teamsByEntity.put(player.getUniqueId(), team);

                if (team.canJoin(player)) {
                    team.addPlayer(player);
                    assigned = true;
                }

                break;
            }
        }

        if (!assigned) {
            this.teamList.get(0).addPlayer(player);
        }
    }

    public void removeEmptyTeams() {
        this.teamList.removeIf(Team::isEmpty);
    }

    public List<Team> getAliveTeams() {
        return this.teamList.stream().filter(Team::isAlive).collect(Collectors.toList());
    }

    public boolean isGameTieOrWin() {
        return this.teamList.stream().filter(Team::isAlive).count() <= 1;
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(this.teamsByEntity.remove(player.getUniqueId())).ifPresent(team -> team.removePlayer(player));
    }

    public Optional<Team> findEntityTeam(LivingEntity entity) {
        return Optional.ofNullable(this.teamsByEntity.getOrDefault(entity.getUniqueId(), null));
    }

    public void reset() {
        this.teamsByEntity.clear();
        this.teamList.clear();
        this.grabTeams();
    }
}
