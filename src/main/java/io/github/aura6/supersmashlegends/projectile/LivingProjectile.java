package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public abstract class LivingProjectile<T extends LivingEntity> extends EmulatedProjectile<T> {

    public LivingProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    @Override
    public void onLaunch() {
        plugin.getTeamManager().getPlayerTeam(this.launcher).addEntity(this.entity);
    }

    @Override
    public void onRemove() {
        plugin.getTeamManager().getPlayerTeam(this.launcher).removeEntity(this.entity);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() == entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() == entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCustomDamage(DamageEvent event) {
        if (event.getVictim() == entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() == entity) {
            remove(ProjectileRemoveReason.ENTITY_DEATH);
        }
    }
}
