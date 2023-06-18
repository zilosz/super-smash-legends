package io.github.aura6.supersmashlegends.event.projectile;

import io.github.aura6.supersmashlegends.projectile.CustomProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class ProjectileHitBlockEvent extends ProjectileEvent {
    @Getter private final BlockHitResult result;

    public ProjectileHitBlockEvent(CustomProjectile<? extends Entity> projectile, BlockHitResult result) {
        super(projectile);
        this.result = result;
    }
}
