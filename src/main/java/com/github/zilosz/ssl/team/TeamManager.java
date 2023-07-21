package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.effects.ColorType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamManager {
    private List<Team> teamList;
    private Map<LivingEntity, Team> teamsByEntity;

    public void setupTeams() {
        this.teamList = new ArrayList<>();

        for (ColorType colorType : ColorType.values()) {
            this.teamList.add(new Team(colorType, this.getDefaultTeamSize()));
        }

        this.teamsByEntity = new HashMap<>();
    }

    public int getDefaultTeamSize() {
        return this.getConfig().getInt("Size");
    }

    private Section getConfig() {
        return SSL.getInstance().getResources().getConfig().getSection("Teams");
    }

    public int getAbsolutePlayerCap() {
        return this.teamList.stream().mapToInt(Team::getPlayerCap).sum();
    }

    public List<Team> getTeamList() {
        return Collections.unmodifiableList(this.teamList);
    }

    public String getPlayerColor(Player player) {
        if (this.isTeamsModeEnabled()) {
            return this.getEntityTeam(player).getColorType().getChatSymbol();
        }
        return SSL.getInstance().getGameManager().getProfile(player).getKit().getColor().getChatSymbol();
    }

    public boolean isTeamsModeEnabled() {
        return this.getConfig().getBoolean("Enabled");
    }

    public Team getEntityTeam(LivingEntity entity) {
        return this.teamsByEntity.get(entity);
    }

    public void removeEntityFromTeam(LivingEntity entity) {
        Optional.ofNullable(this.teamsByEntity.remove(entity)).ifPresent(previous -> previous.removeEntity(entity));
    }

    public void addEntityToTeam(LivingEntity entity, LivingEntity entityWithTeam) {
        this.addEntityToTeam(entity, this.getEntityTeam(entityWithTeam));
    }

    public void addEntityToTeam(LivingEntity entity, Team team) {
        this.teamsByEntity.put(entity, team);
        team.addEntity(entity);
    }

    public void assignPlayer(Player player) {
        if (this.getEntityTeam(player) != null) return;

        this.teamList.stream()
                .filter(team -> team.getPlayerCount() < team.getPlayerCap())
                .findAny().ifPresent(team -> this.addEntityToTeam(player, team));
    }

    public void removeEmptyTeams() {
        this.teamList.removeIf(team -> team.getPlayerCount() == 0);
    }

    public List<Team> getAliveTeams() {
        return this.teamList.stream().filter(Team::isAlive).collect(Collectors.toList());
    }

    public boolean hasGameEnded() {
        return this.teamList.stream().filter(Team::isAlive).count() <= 1;
    }
}
