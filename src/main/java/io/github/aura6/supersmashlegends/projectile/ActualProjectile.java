package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.utils.block.BlockUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public abstract class ActualProjectile<T extends Projectile> extends CustomProjectile<T> {

    public ActualProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
        this.removeOnBlockHit = true;
    }

    public abstract T createProjectile(Location location);

    @Override
    public T createEntity(Location location) {
        T projectile = createProjectile(location);
        projectile.setShooter(this.ability.getPlayer());
        return projectile;
    }

    @EventHandler
    public void onTargetHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() != this.entity) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);

        if (event.getEntity() != this.launcher) {
            this.handleTargetHit((LivingEntity) event.getEntity());
            this.remove();
        }
    }

    public void onGeneralHit() {}

    @EventHandler
    public void handleBlockHit(ProjectileHitEvent event) {
        if (event.getEntity() != this.entity) return;

        this.onGeneralHit();

        Section collisionConfig = this.plugin.getResources().getConfig().getSection("Collision");
        int range = collisionConfig.getInt("CheckRange");
        double step = collisionConfig.getDouble("CheckStep");
        double faceAccuracy = collisionConfig.getDouble("FaceAccuracy");

        this.handleBlockHitResult(BlockUtils.findBlockHitWithRay(this.entity, this.entity.getVelocity(), range, step, faceAccuracy));
    }
}
