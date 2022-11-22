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
        projectile.setShooter(ability.getPlayer());
        return projectile;
    }

    @EventHandler
    public void checkTargetHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() != entity) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        if (event.getEntity() == ability.getPlayer()) {
            event.setCancelled(true);

        } else {
            handleTargetHit((LivingEntity) event.getEntity());
            remove();
        }
    }

    public void onGeneralHit() {}

    @EventHandler
    public void handleBlockHit(ProjectileHitEvent event) {
        if (event.getEntity() != entity) return;

        onGeneralHit();

        Section collisionConfig = plugin.getResources().getConfig().getSection("Collision");
        int range = collisionConfig.getInt("CheckRange");
        double step = collisionConfig.getDouble("CheckStep");
        double faceAccuracy = collisionConfig.getDouble("FaceAccuracy");

        handleBlockHitResult(BlockUtils.findBlockHitWithRay(entity, entity.getVelocity(), range, step, faceAccuracy));
    }
}
