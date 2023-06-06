package io.github.aura6.supersmashlegends.event.projectile;

import io.github.aura6.supersmashlegends.event.CustomEvent;
import io.github.aura6.supersmashlegends.projectile.CustomProjectile;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class ProjectileEvent extends CustomEvent {
    @Getter private final CustomProjectile<? extends Entity> projectile;

    public ProjectileEvent(CustomProjectile<? extends Entity> projectile) {
        this.projectile = projectile;
    }
}
