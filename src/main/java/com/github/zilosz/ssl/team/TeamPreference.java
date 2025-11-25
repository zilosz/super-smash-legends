package com.github.zilosz.ssl.team;

import org.bukkit.entity.LivingEntity;

@FunctionalInterface
public interface TeamPreference {
  TeamPreference ENEMY = (team, entity) -> !team.hasEntity(entity);
  TeamPreference ANY = (team, entity) -> true;

  boolean validate(Team team, LivingEntity entity);
}
