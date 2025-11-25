package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.util.block.BlockUtils;
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
    removeOnBlockHit = true;
    recreateOnBounce = true;
    useCustomHitBox = false;
  }

  @Override
  protected T createEntity(Location location) {
    T projectile = createProjectile(location);
    projectile.setShooter(launcher);
    return projectile;
  }

  protected abstract T createProjectile(Location location);

  @EventHandler
  public void onTargetHit(EntityDamageByEntityEvent event) {
    if (event.getDamager() != entity) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;

    event.setCancelled(true);

    if (event.getEntity() != launcher) {
      hitTarget((LivingEntity) event.getEntity());
    }
  }

  @EventHandler
  public void onBlockHit(ProjectileHitEvent event) {
    if (event.getEntity() != entity) return;

    onGeneralHit();

    Section collisionConfig = SSL.getInstance().getResources().getConfig().getSection("Collision");
    int range = collisionConfig.getInt("CheckRange");
    double step = collisionConfig.getDouble("CheckStep");
    double faceAccuracy = collisionConfig.getDouble("FaceAccuracy");

    Vector velocity = entity.getVelocity();
    hitBlock(BlockUtils.findBlockHitWithRay(entity, velocity, range, step, faceAccuracy));
  }

  protected void onGeneralHit() {}
}
