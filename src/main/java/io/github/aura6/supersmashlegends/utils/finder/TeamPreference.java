package io.github.aura6.supersmashlegends.utils.finder;

import io.github.aura6.supersmashlegends.team.Team;
import org.bukkit.entity.LivingEntity;

public interface TeamPreference {
    TeamPreference FRIENDLY = Team::hasEntity;
    TeamPreference ENEMY = (team, target) -> !team.hasEntity(target);
    TeamPreference ANY = (team, target) -> true;

    boolean validate(Team team, LivingEntity target);
}
