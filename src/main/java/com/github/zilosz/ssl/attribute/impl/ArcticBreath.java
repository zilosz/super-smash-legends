package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class ArcticBreath extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    Location eyeLoc = player.getEyeLocation();

    int ringCount = config.getInt("RingCount");
    Vector step = eyeLoc.getDirection().multiply(config.getDouble("Range") / ringCount);

    double maxDamage = config.getDouble("MaxDamage");
    double damageDiff = maxDamage - config.getDouble("MinDamage");
    double damageStep = damageDiff / (ringCount - 1);

    double minRadius = config.getDouble("MinRadius");
    double radiusDiff = config.getDouble("MaxRadius") - minRadius;
    double radiusStep = radiusDiff / (ringCount - 1);

    createRing(eyeLoc, step, maxDamage, damageStep, minRadius, radiusStep, 0);
  }

  public void createRing(
      Location center,
      Vector step,
      double damage,
      double damageStep,
      double radius,
      double radiusStep,
      int ringCount
  ) {
    if (ringCount == config.getInt("RingCount")) return;

    player.getWorld().playSound(center, Sound.GLASS, 2, 2);

    double density = config.getDouble("ParticleDensity");
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SNOW_SHOVEL);
    new ParticleMaker(particle).ring(center, radius, density);

    EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

    new EntityFinder(selector).findAll(player, center).forEach(target -> {
      Attack attack = YamlReader.attack(config, step, getDisplayName());
      AttackInfo attackInfo = new AttackInfo(AttackType.ARCTIC_BREATH, this);
      SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
    });

    createRing(
        center.add(step),
        step,
        damage - damageStep,
        damageStep,
        radius + radiusStep,
        radiusStep,
        ringCount + 1
    );
  }
}
