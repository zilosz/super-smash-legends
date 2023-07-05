package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public abstract class LivingProjectile<T extends LivingEntity> extends EmulatedProjectile<T> {

    public LivingProjectile(Ability ability, Section config) {
        super(ability, config);
    }

    @Override
    public void onLaunch() {
        SSL.getInstance().getTeamManager().getPlayerTeam(this.launcher).addEntity(this.entity);
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
        SSL.getInstance().getTeamManager().getPlayerTeam(this.launcher).removeEntity(this.entity);
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
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
            this.remove(ProjectileRemoveReason.ENTITY_DEATH);
        }
    }
}
