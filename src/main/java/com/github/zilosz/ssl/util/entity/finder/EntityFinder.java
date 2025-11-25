package com.github.zilosz.ssl.util.entity.finder;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityFinder {
  private final EntitySelector rangeSelector;
  private final Collection<LivingEntity> entitiesToAvoid = new HashSet<>();
  private TeamPreference teamPreference = TeamPreference.ENEMY;
  private boolean hitsUser;

  public EntityFinder(EntitySelector rangeSelector) {
    this.rangeSelector = rangeSelector;
  }

  public EntityFinder setTeamPreference(TeamPreference teamSelection) {
    teamPreference = teamSelection;
    return this;
  }

  public EntityFinder setAvoidsUser(boolean avoidsUser) {
    hitsUser = !avoidsUser;
    return this;
  }

  public EntityFinder avoid(LivingEntity target) {
    entitiesToAvoid.add(target);
    return this;
  }

  public EntityFinder avoidAll(Collection<LivingEntity> targets) {
    entitiesToAvoid.addAll(targets);
    return this;
  }

  public Optional<LivingEntity> findClosest(Player user) {
    return findClosest(user, user.getLocation());
  }

  public Optional<LivingEntity> findClosest(Player user, Location location) {
    return getFilteredStream(
        user,
        location
    ).min(Comparator.comparingDouble(entity -> location.distanceSquared(entity.getLocation())));
  }

  private Stream<LivingEntity> getFilteredStream(Player user, Location location) {
    return rangeSelector
        .getEntityStream(location)
        .filter(entity -> entity.getType() != EntityType.ARMOR_STAND)
        .filter(LivingEntity.class::isInstance)
        .map(LivingEntity.class::cast)
        .filter(this::isValidPlayer)
        .filter(entity -> hitsUser || entity != user)
        .filter(entity -> !entitiesToAvoid.contains(entity))
        .filter(entity -> isCorrectTeam(user, entity));
  }

  private boolean isValidPlayer(LivingEntity entity) {
    if (!(entity instanceof Player) || SSL.getInstance().getNpcRegistry().isNPC(entity)) {
      return true;
    }
    Player player = (Player) entity;
    return SSL.getInstance().getGameManager().isPlayerAlive(player) &&
           player.getGameMode() == GameMode.SURVIVAL;
  }

  private boolean isCorrectTeam(Player user, LivingEntity entity) {
    return teamPreference.validate(SSL.getInstance().getTeamManager().getEntityTeam(user), entity);
  }

  public Set<LivingEntity> findAll(Player user) {
    return findAll(user, user.getLocation());
  }

  public Set<LivingEntity> findAll(Player user, Location location) {
    return getFilteredStream(user, location).collect(Collectors.toSet());
  }
}
