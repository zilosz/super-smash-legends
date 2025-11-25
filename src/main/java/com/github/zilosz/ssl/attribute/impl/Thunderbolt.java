package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Thunderbolt extends ChargedRightClickAbility {

  @Override
  public void onFailedCharge() {
    player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
  }

  @Override
  public void onSuccessfulCharge() {
    player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2, 0.5f);

    EntityFinder finder = new EntityFinder(new HitBoxSelector(config.getDouble("HitBox")));

    int ticks = ticksCharging - getMinChargeTicks();
    int max = getMaxChargeTicks() - getMinChargeTicks();

    double damage = YamlReader.incVal(config, "Damage", ticks, max);
    double kb = YamlReader.incVal(config, "Kb", ticks, max);
    double range = YamlReader.incVal(config, "Range", ticks, max);

    Location location = player.getEyeLocation();
    Vector step = location.getDirection().multiply(0.25);

    boolean found = false;
    double stepped = 0;

    while (true) {

      if (stepped > range || location.getBlock().getType().isSolid() || found) {
        endEffect(location);
        break;
      }

      for (LivingEntity target : finder.findAll(player, location)) {
        Attack attack = YamlReader.attack(config, step, getDisplayName());
        attack.getDamage().setDamage(damage);
        attack.getKb().setKb(kb);

        AttackInfo attackInfo = new AttackInfo(AttackType.THUNDERBOLT, this);

        if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
          found = true;
          break;
        }
      }

      ParticleBuilder particle =
          new ParticleBuilder(ParticleEffect.REDSTONE).setColor(kit.getColor().getAwtColor());
      new ParticleMaker(particle).show(location);

      stepped += 0.25;
      location.add(step);
    }
  }

  @Override
  public void onChargeTick() {
    player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1, 2);
  }

  private void endEffect(Location location) {
    location.getWorld().strikeLightningEffect(location);
    location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);

    FireworkEffect.Builder settings = FireworkEffect
        .builder()
        .withColor(kit.getColor().getBukkitColor())
        .with(FireworkEffect.Type.BALL)
        .withTrail();

    Firework firework = Effects.launchFirework(location, settings, 1);
    Bukkit.getScheduler().runTaskLater(SSL.getInstance(), firework::detonate, 5);
  }
}
