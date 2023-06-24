package com.github.zilosz.ssl.utils.entity.finder;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityFinder {
    private final EntitySelector rangeSelector;
    private final List<LivingEntity> toAvoid = new ArrayList<>();
    private TeamPreference teamPreference = TeamPreference.ENEMY;
    private boolean avoidsUser = true;
    private EntityType entityType;

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
        this.toAvoid.add(target);
        return this;
    }

    public EntityFinder setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public Optional<LivingEntity> findClosest(LivingEntity user) {
        return this.findClosest(user, user.getLocation());
    }

    public Optional<LivingEntity> findClosest(LivingEntity user, Location location) {
        return this.getFilteredStream(user, location).min(Comparator.comparingDouble(entity -> location.distanceSquared(
                entity.getLocation())));
    }

    private Stream<LivingEntity> getFilteredStream(LivingEntity user, Location location) {
        return this.rangeSelector.getEntityStream(location)
                .filter(entity -> !CitizensAPI.getNPCRegistry().isNPC(entity))
                .filter(entity -> this.entityType == null || entity.getType().equals(this.entityType))
                .filter(entity -> !entity.getType().equals(EntityType.ARMOR_STAND))
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> !(entity instanceof Player) || SSL.getInstance().getGameManager().isPlayerAlive((Player) entity) && ((HumanEntity) entity).getGameMode() == GameMode.SURVIVAL)
                .filter(entity -> !this.avoidsUser || entity != user)
                .filter(entity -> !this.toAvoid.contains(entity))
                .filter(entity -> SSL.getInstance().getTeamManager().findEntityTeam(user).map(team -> this.teamPreference.validate(team, entity)).orElse(false));
    }

    public Set<LivingEntity> findAll(LivingEntity user) {
        return this.findAll(user, user.getLocation());
    }

    public Set<LivingEntity> findAll(LivingEntity user, Location location) {
        return this.getFilteredStream(user, location).collect(Collectors.toSet());
    }
}
