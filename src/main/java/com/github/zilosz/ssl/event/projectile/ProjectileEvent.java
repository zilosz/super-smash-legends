package com.github.zilosz.ssl.event.projectile;

import com.github.zilosz.ssl.event.CustomEvent;
import com.github.zilosz.ssl.projectile.CustomProjectile;
import lombok.Getter;
import org.bukkit.entity.Entity;

@Getter
public class ProjectileEvent extends CustomEvent {
    private final CustomProjectile<? extends Entity> projectile;

    public ProjectileEvent(CustomProjectile<? extends Entity> projectile) {
        this.projectile = projectile;
    }
}
