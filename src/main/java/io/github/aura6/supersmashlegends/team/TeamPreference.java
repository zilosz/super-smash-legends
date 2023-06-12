package io.github.aura6.supersmashlegends.team;

import org.bukkit.entity.LivingEntity;

public interface TeamPreference {
    TeamPreference FRIENDLY = Team::hasAnyEntity;
    TeamPreference ENEMY = (team, livingEntity) -> !team.hasAnyEntity(livingEntity);
    TeamPreference ANY = (team, livingEntity) -> true;

    boolean validate(Team team, LivingEntity livingEntity);
}
