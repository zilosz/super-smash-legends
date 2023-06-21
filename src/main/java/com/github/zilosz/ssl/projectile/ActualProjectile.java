package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public abstract class ActualProjectile<T extends Projectile> extends CustomProjectile<T> {

    public ActualProjectile(SSL plugin, Ability ability, Section config) {
        super(plugin, ability, config);
        this.removeOnBlockHit = true;
    }

    @Override
    public T createEntity(Location location) {
        T projectile = this.createProjectile(location);
        projectile.setShooter(this.ability.getPlayer());
        return projectile;
    }

    public abstract T createProjectile(Location location);

    @EventHandler
    public void onTargetHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() != this.entity) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);

        if (event.getEntity() != this.launcher) {
            this.handleTargetHit((LivingEntity) event.getEntity());
        }
    }

    @EventHandler
    public void handleBlockHit(ProjectileHitEvent event) {
        if (event.getEntity() != this.entity) return;

        this.onGeneralHit();

        Section collisionConfig = this.plugin.getResources().getConfig().getSection("Collision");
        int range = collisionConfig.getInt("CheckRange");
        double step = collisionConfig.getDouble("CheckStep");
        double faceAccuracy = collisionConfig.getDouble("FaceAccuracy");

        this.handleBlockHitResult(BlockUtils.findBlockHitWithRay(
                this.entity,
                this.entity.getVelocity(),
                range,
                step,
                faceAccuracy
        ));
    }

    public void onGeneralHit() {}
}
