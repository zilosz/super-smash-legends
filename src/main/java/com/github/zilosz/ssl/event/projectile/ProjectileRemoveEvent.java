package com.github.zilosz.ssl.event.projectile;

import com.github.zilosz.ssl.projectile.CustomProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;

@Getter
public class ProjectileRemoveEvent extends ProjectileEvent implements Cancellable {
    private final ProjectileRemoveReason reason;
    @Setter private boolean cancelled = false;

    public ProjectileRemoveEvent(CustomProjectile<? extends Entity> projectile, ProjectileRemoveReason reason) {
        super(projectile);
        this.reason = reason;
    }
}
