package com.github.zilosz.ssl.event.projectile;

import com.github.zilosz.ssl.projectile.CustomProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class ProjectileHitBlockEvent extends ProjectileEvent {
    @Getter private final BlockHitResult result;

    public ProjectileHitBlockEvent(CustomProjectile<? extends Entity> projectile, BlockHitResult result) {
        super(projectile);
        this.result = result;
    }
}
