package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.event.attack.AttributeDamageEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public abstract class LivingProjectile<T extends LivingEntity> extends EmulatedProjectile<T> {

    public LivingProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    @Override
    public void onLaunch() {
        this.plugin.getTeamManager().getPlayerTeam(this.launcher).addEntity(this.entity);
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
        this.plugin.getTeamManager().getPlayerTeam(this.launcher).removeEntity(this.entity);
    }

    @EventHandler
    public void onDamage(AttributeDamageEvent event) {
        if (event.getVictim() == this.entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() == this.entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() == this.entity) {
            remove(ProjectileRemoveReason.ENTITY_DEATH);
        }
    }
}
