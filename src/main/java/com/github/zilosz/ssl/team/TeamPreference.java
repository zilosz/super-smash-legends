package com.github.zilosz.ssl.team;

import org.bukkit.entity.LivingEntity;

public interface TeamPreference {
    TeamPreference FRIENDLY = Team::hasAnyEntity;
    TeamPreference ENEMY = (team, entity) -> !team.hasAnyEntity(entity);
    TeamPreference ANY = (team, entity) -> true;

    boolean validate(Team team, LivingEntity livingEntity);
}
