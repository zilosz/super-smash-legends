package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public abstract class ActualProjectile<T extends Projectile> extends CustomProjectile<T> {

    public ActualProjectile(Section config, AttackInfo attackInfo) {
        super(config, attackInfo);
        this.removeOnBlockHit = true;
        this.recreateOnBounce = true;
        this.useCustomHitBox = false;
        this.removeOnFailedHit = true;
    }

    @Override
    protected T createEntity(Location location) {
        T projectile = this.createProjectile(location);
        projectile.setShooter(this.launcher);
        return projectile;
    }

    protected abstract T createProjectile(Location location);

    @EventHandler
    public void onTargetHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() != this.entity) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);

        if (event.getEntity() != this.launcher) {
            this.hitTarget((LivingEntity) event.getEntity());
        }
    }

    @EventHandler
    public void handleBlockHit(ProjectileHitEvent event) {
        if (event.getEntity() != this.entity) return;

        this.onGeneralHit();

        Section collisionConfig = SSL.getInstance().getResources().getConfig().getSection("Collision");
        int range = collisionConfig.getInt("CheckRange");
        double step = collisionConfig.getDouble("CheckStep");
        double faceAccuracy = collisionConfig.getDouble("FaceAccuracy");

        Vector velocity = this.entity.getVelocity();
        this.hitBlock(BlockUtils.findBlockHitWithRay(this.entity, velocity, range, step, faceAccuracy));
    }

    protected void onGeneralHit() {}
}
