package io.github.aura6.supersmashlegends.team;

import org.bukkit.entity.LivingEntity;

public interface TeamPreference {
    TeamPreference FRIENDLY = Team::hasEntity;
    TeamPreference ENEMY = (team, livingEntity) -> !team.hasEntity(livingEntity);
    TeamPreference ANY = (team, livingEntity) -> true;

    boolean validate(Team team, LivingEntity livingEntity);
}
