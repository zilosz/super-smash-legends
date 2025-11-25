package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class AxeThrow extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    AttackInfo attackInfo = new AttackInfo(AttackType.AXE_THROW, this);
    new AxeProjectile(config.getSection("Projectile"), attackInfo).launch();
    player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 2, 1);
    hotbarItem.hide();
  }

  @Override
  public void onCooldownEnd() {
    hotbarItem.show();
  }

  private static class AxeProjectile extends ItemProjectile {

    public AxeProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
      ((Ability) attackInfo.getAttribute()).getHotbarItem().show();
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      entity.getWorld().playSound(entity.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
    }

    @Override
    public void onTick() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).show(entity.getLocation());
    }
  }
}
