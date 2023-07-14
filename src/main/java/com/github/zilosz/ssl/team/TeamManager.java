package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.effects.ColorType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamManager {
    private final List<Team> teamList = new ArrayList<>();
    private final Map<UUID, Team> teamsByEntity = new HashMap<>();

    public TeamManager() {
        this.loadTeams();
    }

    private void loadTeams() {
        for (ColorType colorType : ColorType.values()) {
            this.teamList.add(new Team(colorType));
        }
    }

    public int getAbsolutePlayerCap() {
        return ColorType.values().length * this.getTeamSize();
    }

    public int getTeamSize() {
        return SSL.getInstance().getResources().getConfig().getInt("Game.TeamSize");
    }

    public List<Team> getTeamList() {
        return Collections.unmodifiableList(this.teamList);
    }

    public Optional<Team> findChosenTeam(Player player) {
        return this.teamList.stream().filter(team -> team.hasPlayer(player)).findAny();
    }

    public String getPlayerColor(Player player) {
        if (this.getTeamSize() == 1) {
            return SSL.getInstance().getGameManager().getProfile(player).getKit().getColor().getChatSymbol();
        }
        return this.getPlayerTeam(player).getColorType().getChatSymbol();
    }

    public Team getPlayerTeam(Player player) {
        return this.teamsByEntity.get(player.getUniqueId());
    }

    public void assignPlayer(Player player) {
        boolean assigned = false;

        for (Team team : this.teamList) {

            if (team.hasPlayer(player) || team.canJoin(player)) {
                this.teamsByEntity.put(player.getUniqueId(), team);
                assigned = true;

                if (team.canJoin(player)) {
                    team.addPlayer(player);
                }

                break;
            }
        }

        if (!assigned) {
            this.teamList.get(0).addPlayer(player);
        }
    }

    public void removeEmptyTeams() {
        this.teamList.removeIf(Team::hasNoPlayers);
    }

    public List<Team> getAliveTeams() {
        return this.teamList.stream().filter(Team::isAlive).collect(Collectors.toList());
    }

    public boolean isGameTieOrWin() {
        return this.teamList.stream().filter(Team::isAlive).count() <= 1;
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(this.teamsByEntity.remove(player.getUniqueId()))
                .ifPresent(team -> team.removePlayer(player));
    }

    public Optional<Team> findEntityTeam(LivingEntity entity) {
        return Optional.ofNullable(this.teamsByEntity.getOrDefault(entity.getUniqueId(), null));
    }

    public void reset() {
        this.teamsByEntity.clear();
        this.teamList.clear();
        this.loadTeams();
    }
}
