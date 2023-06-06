package io.github.aura6.supersmashlegends.event.projectile;

import io.github.aura6.supersmashlegends.projectile.CustomProjectile;
import io.github.aura6.supersmashlegends.projectile.ProjectileRemoveReason;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;

public class ProjectileRemoveEvent extends ProjectileEvent implements Cancellable {
    @Getter @Setter private boolean cancelled = false;
    @Getter private final ProjectileRemoveReason reason;

    public ProjectileRemoveEvent(CustomProjectile<? extends Entity> projectile, ProjectileRemoveReason reason) {
        super(projectile);
        this.reason = reason;
    }
}
