package com.github.zilosz.ssl.attribute.impl;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.SoundCanceller;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class WebGrapple extends RightClickAbility {
  private SoundCanceller batSoundCanceller;
  private GrappleProjectile grappleProjectile;

  @Override
  public void activate() {
    super.activate();

    batSoundCanceller = new SoundCanceller(SSL.getInstance(), "mob.bat.idle");
    ProtocolLibrary.getProtocolManager().addPacketListener(batSoundCanceller);
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    AttackInfo attackInfo = new AttackInfo(AttackType.WEB_GRAPPLE, this);
    grappleProjectile = new GrappleProjectile(config.getSection("Projectile"), attackInfo);
    grappleProjectile.launch();

    player.getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 1);
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (batSoundCanceller != null) {
      ProtocolLibrary.getProtocolManager().removePacketListener(batSoundCanceller);
    }

    if (grappleProjectile != null) {
      grappleProjectile.remove(ProjectileRemoveReason.DEACTIVATION);
    }
  }

  private static class GrappleProjectile extends ItemProjectile {
    private Bat bat;

    public GrappleProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public Item createEntity(Location location) {
      Item item = super.createEntity(location);
      bat = location.getWorld().spawn(launcher.getEyeLocation(), Bat.class);
      new PotionEffectEvent(bat, PotionEffectType.INVISIBILITY, 10_000, 1).apply();
      bat.setLeashHolder(item);
      launcher.setPassenger(bat);
      return item;
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      pull();
      launcher.playSound(launcher.getLocation(), Sound.DIG_WOOD, 1, 1);
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
      bat.remove();
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      pull();
    }

    private void pull() {
      Vector direction = VectorUtils.fromTo(launcher, entity).normalize();
      Vector extraY = new Vector(0, config.getDouble("ExtraY"), 0);
      launcher.setVelocity(direction.multiply(config.getDouble("PullSpeed")).add(extraY));
    }

    @EventHandler
    public void onBatAttack(AttackEvent event) {
      if (event.getVictim() == bat) {
        event.setCancelled(true);
      }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
      if (event.getEntity() == bat) {
        remove(ProjectileRemoveReason.ENTITY_DEATH);
      }
    }
  }
}
