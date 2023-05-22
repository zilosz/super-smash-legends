package io.github.aura6.supersmashlegends.utils.finder;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.finder.range.RangeSelector;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityFinder {
    private final SuperSmashLegends plugin;
    private final RangeSelector rangeSelector;
    private TeamPreference teamPreference = TeamPreference.ENEMY;
    private boolean avoidsUser = true;
    private EntityType entityType;
    private final List<UUID> toAvoid = new ArrayList<>();

    public EntityFinder(SuperSmashLegends plugin, RangeSelector rangeSelector) {
        this.plugin = plugin;
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

    public EntityFinder avoid(Entity target) {
        toAvoid.add(target.getUniqueId());
        return this;
    }

    public EntityFinder setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    private Stream<LivingEntity> getFilteredStream(LivingEntity user, Location location) {
        return rangeSelector.getEntityStream(location)
                .filter(entity -> entityType == null || entity.getType() == entityType)
                .filter(entity -> !(entity instanceof ArmorStand))
                .filter(entity -> entity instanceof LivingEntity)
                .map(LivingEntity.class::cast)
                .filter(entity -> !(entity instanceof Player) || plugin.getGameManager().isPlayerAlive((Player) entity))
                .filter(entity -> !avoidsUser || entity != user)
                .filter(entity -> !toAvoid.contains(entity.getUniqueId()))
                .filter(entity -> plugin.getTeamManager().findEntityTeam(user).map(team -> teamPreference.validate(team, entity)).orElse(true));
    }

    public Optional<LivingEntity> findClosest(LivingEntity user, Location location) {
        return getFilteredStream(user, location).min(Comparator.comparingDouble(entity -> location.distanceSquared(entity.getLocation())));
    }

    public Optional<LivingEntity> findClosest(LivingEntity user) {
        return findClosest(user, user.getLocation());
    }

    public Set<LivingEntity> findAll(LivingEntity user, Location location) {
        return getFilteredStream(user, location).collect(Collectors.toSet());
    }

    public Set<LivingEntity> findAll(LivingEntity user) {
        return findAll(user, user.getLocation());
    }
}