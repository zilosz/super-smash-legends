package com.github.zilosz.ssl.event.projectile;

import com.github.zilosz.ssl.projectile.CustomProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import lombok.Getter;

@Getter
public class ProjectileHitBlockEvent extends ProjectileEvent {
    private final BlockHitResult result;

    public ProjectileHitBlockEvent(CustomProjectile<?> projectile, BlockHitResult result) {
        super(projectile);
        this.result = result;
    }
}
