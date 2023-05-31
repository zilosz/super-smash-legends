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
    private final List<Team> teamList = new ArrayList<>();
    private final Map<UUID, Team> teamsByEntity = new HashMap<>();

    public TeamManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
        grabTeams();
    }

    private void grabTeams() {
        TEAM_COLORS.subList(0, getTeamCount()).forEach(colors -> teamList.add(new Team(plugin, colors)));
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

    public int getPlayerStartCount() {
        return getTeamCount() * getTeamSize();
    }

    public int getAbsolutePlayerCap() {
        return TEAM_COLORS.size() * getTeamSize();
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
        return getTeamSize() == 1 ? this.plugin.getGameManager().getProfile(player).getKit().getColor() : getPlayerTeam(player).getColor();
    }

    public void assignPlayer(Player player) {

        for (Team team : this.teamList) {

            if (team.hasPlayer(player) || team.canJoin(player)) {
                this.teamsByEntity.put(player.getUniqueId(), team);

                if (team.canJoin(player)) {
                    team.addPlayer(player);
                }

                break;
            }
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
        this.teamList.clear();
        this.teamsByEntity.clear();
        grabTeams();
    }
}
