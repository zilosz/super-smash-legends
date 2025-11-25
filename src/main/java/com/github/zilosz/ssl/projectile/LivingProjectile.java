package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public abstract class LivingProjectile<T extends LivingEntity> extends EmulatedProjectile<T> {

  public LivingProjectile(Section config, AttackInfo attackInfo) {
    super(config, attackInfo);
  }

  @Override
  public void onLaunch() {
    SSL.getInstance().getTeamManager().addEntityToTeam(entity, launcher);
  }

  @Override
  public void onRemove(ProjectileRemoveReason reason) {
    SSL.getInstance().getTeamManager().removeEntityFromTeam(entity);
  }

  @EventHandler
  public void onDamage(DamageEvent event) {
    if (event.getVictim() == entity) {
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
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity() == entity) {
      remove(ProjectileRemoveReason.ENTITY_DEATH);
    }
  }
}
