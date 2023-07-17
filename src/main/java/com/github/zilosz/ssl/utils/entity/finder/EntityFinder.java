package com.github.zilosz.ssl.utils.entity.finder;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
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
    private final Set<LivingEntity> entitiesToAvoid = new HashSet<>();
    private TeamPreference teamPreference = TeamPreference.ENEMY;
    private boolean avoidsUser = true;

    public EntityFinder(EntitySelector rangeSelector) {
        this.rangeSelector = rangeSelector;
    }

    public EntityFinder setTeamPreference(TeamPreference teamSelection) {
        this.teamPreference = teamSelection;
        return this;
    }

    public EntityFinder setAvoidsUser(boolean avoidsUser) {
        this.avoidsUser = avoidsUser;
        return this;
    }

    public EntityFinder avoid(LivingEntity target) {
        this.entitiesToAvoid.add(target);
        return this;
    }

    public EntityFinder avoidAll(Collection<LivingEntity> targets) {
        this.entitiesToAvoid.addAll(targets);
        return this;
    }

    public Optional<LivingEntity> findClosest(Player user) {
        return this.findClosest(user, user.getLocation());
    }

    public Optional<LivingEntity> findClosest(Player user, Location location) {
        return this.getFilteredStream(user, location)
                .min(Comparator.comparingDouble(entity -> location.distanceSquared(entity.getLocation())));
    }

    private Stream<LivingEntity> getFilteredStream(Player user, Location location) {
        return this.rangeSelector.getEntityStream(location)
                .filter(entity -> entity.getType() != EntityType.ARMOR_STAND)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(this::isValidPlayer)
                .filter(entity -> !this.avoidsUser || entity != user)
                .filter(entity -> !this.entitiesToAvoid.contains(entity))
                .filter(entity -> this.isCorrectTeam(user, entity));
    }

    private boolean isValidPlayer(LivingEntity entity) {
        if (!(entity instanceof Player) || SSL.getInstance().getNpcRegistry().isNPC(entity)) return true;
        Player player = (Player) entity;
        return SSL.getInstance().getGameManager().isPlayerAlive(player) && player.getGameMode() == GameMode.SURVIVAL;
    }

    private boolean isCorrectTeam(Player user, LivingEntity entity) {
        return this.teamPreference.validate(SSL.getInstance().getTeamManager().getEntityTeam(user), entity);
    }

    public Set<LivingEntity> findAll(Player user) {
        return this.findAll(user, user.getLocation());
    }

    public Set<LivingEntity> findAll(Player user, Location location) {
        return this.getFilteredStream(user, location).collect(Collectors.toSet());
    }
}
