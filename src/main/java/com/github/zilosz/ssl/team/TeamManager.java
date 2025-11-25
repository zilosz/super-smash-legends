package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.effects.ColorType;
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
    teamList = new ArrayList<>();

    for (ColorType colorType : ColorType.values()) {
      teamList.add(new Team(colorType, getDefaultTeamSize()));
    }

    teamsByEntity = new HashMap<>();
  }

  public int getDefaultTeamSize() {
    return getConfig().getInt("Size");
  }

  private Section getConfig() {
    return SSL.getInstance().getResources().getConfig().getSection("Teams");
  }

  public int getAbsolutePlayerCap() {
    return teamList.stream().mapToInt(Team::getPlayerCap).sum();
  }

  public List<Team> getTeamList() {
    return Collections.unmodifiableList(teamList);
  }

  public String getPlayerColor(Player player) {
    if (isTeamsModeEnabled()) {
      return getEntityTeam(player).getColorType().getChatSymbol();
    }
    return SSL
        .getInstance()
        .getGameManager()
        .getProfile(player)
        .getKit()
        .getColor()
        .getChatSymbol();
  }

  public boolean isTeamsModeEnabled() {
    return getConfig().getBoolean("Enabled");
  }

  public Team getEntityTeam(LivingEntity entity) {
    return teamsByEntity.get(entity);
  }

  public void removeEntityFromTeam(LivingEntity entity) {
    Optional
        .ofNullable(teamsByEntity.remove(entity))
        .ifPresent(previous -> previous.removeEntity(entity));
  }

  public void addEntityToTeam(LivingEntity entity, LivingEntity entityWithTeam) {
    addEntityToTeam(entity, getEntityTeam(entityWithTeam));
  }

  public void addEntityToTeam(LivingEntity entity, Team team) {
    teamsByEntity.put(entity, team);
    team.addEntity(entity);
  }

  public void assignPlayer(Player player) {
    if (getEntityTeam(player) != null) return;

    teamList
        .stream()
        .filter(team -> team.getPlayerCount() < team.getPlayerCap())
        .findAny()
        .ifPresent(team -> addEntityToTeam(player, team));
  }

  public void removeEmptyTeams() {
    teamList.removeIf(team -> team.getPlayerCount() == 0);
  }

  public List<Team> getAliveTeams() {
    return teamList.stream().filter(Team::isAlive).collect(Collectors.toList());
  }

  public boolean hasGameEnded() {
    return teamList.stream().filter(Team::isAlive).count() <= 1;
  }
}
