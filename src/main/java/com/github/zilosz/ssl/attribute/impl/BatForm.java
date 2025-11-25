package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.Noise;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.DisguiseUtils;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.Set;
import java.util.stream.Collectors;

public class BatForm extends PassiveAbility {
  private int oldJumpLimit;
  private Set<Attribute> removedAttributes;
  private boolean isBat;

  @EventHandler
  public void onAttributeDamage(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (event.getInfo().getType() != AttackType.MELEE) return;
    if (!isBat) return;
    if (!RegenEvent.attempt(player, config.getDouble("Regen"))) return;

    Location center = EntityUtils.center(event.getVictim());
    player.getWorld().playSound(center, Sound.ZOMBIE_UNFECT, 1, 2);

    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
    new ParticleMaker(particle).boom(SSL.getInstance(), center, 3, 0.3, 7);
  }

  @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true)
  public void onDamage(DamageEvent event) {
    if (event.getVictim() != player) return;

    if (isBat) {
      double multiplier = config.getDouble("DamageTakenMultiplier");
      event.getDamage().setDamage(event.getDamage().getDamage() * multiplier);
      return;
    }

    if (event.getNewHealth() <= config.getDouble("HealthThreshold")) {
      isBat = true;

      player.getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1, 0.5f);
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);

      for (int i = 0; i < 3; i++) {
        new ParticleMaker(particle).setSpread(0.5).show(player.getLocation());
      }

      Disguise disguise =
          DisguiseUtils.applyDisguiseParams(player, new MobDisguise(DisguiseType.BAT));
      DisguiseAPI.disguiseToAll(player, disguise);

      oldJumpLimit = kit.getJump().getMaxCount();
      kit.getJump().setMaxCount(config.getInt("ExtraJumps"));
      kit.getJump().replenish();

      removedAttributes = kit
          .getAttributes()
          .stream()
          .filter(ClickableAbility.class::isInstance)
          .collect(Collectors.toSet());

      removedAttributes.forEach(Attribute::destroy);
      player.getInventory().setItem(0, new ItemBuilder<>(Material.GOLD_SWORD).get());
    }
  }

  @Override
  public void deactivate() {
    reset();
    super.deactivate();
  }

  @Override
  public String getUseType() {
    return "Enter Low Health";
  }

  private void reset() {
    if (!isBat) return;

    isBat = false;
    player.getInventory().remove(Material.GOLD_SWORD);
    kit.getJump().setMaxCount(oldJumpLimit);
    DisguiseAPI.undisguiseToAll(player);
  }

  @EventHandler
  public void onRegen(RegenEvent event) {
    if (event.getPlayer() != player) return;
    if (!isBat) return;

    event.setRegen(event.getRegen() * config.getDouble("RegenMultiplier"));

    if (player.getHealth() + event.getRegen() <= config.getDouble("HealthThreshold")) {
      return;
    }

    reset();

    for (Attribute attribute : removedAttributes) {
      attribute.equip();
      attribute.activate();
    }

    player.getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1, 2);

    for (int i = 0; i < 3; i++) {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
      new ParticleMaker(particle).setSpread(0.3).show(player.getLocation());
    }
  }

  @EventHandler
  public void onJump(DoubleJumpEvent event) {
    if (event.getPlayer() != player) return;
    if (!isBat) return;

    event.setPower(event.getPower() * config.getDouble("JumpPowerMultiplier"));
    event.setHeight(event.getHeight() * config.getDouble("JumpHeightMultiplier"));
    event.setNoise(new Noise(Sound.BAT_TAKEOFF, 1, 2));
  }
}
