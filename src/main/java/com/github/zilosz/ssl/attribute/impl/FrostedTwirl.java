package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class FrostedTwirl extends ChargedRightClickAbility {

  @Override
  public void onChargeTick() {
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SNOW_SHOVEL);
    new ParticleMaker(particle).ring(EntityUtils.center(player), 90, 0, 1, 20);

    player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 2, 1.5f);

    Vector forward = player.getEyeLocation().getDirection().multiply(config.getDouble("Velocity"));
    player.setVelocity(forward.setY(config.getDouble("VelocityY")));

    EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

    new EntityFinder(selector).findAll(player).forEach(target -> {
      Attack attack = YamlReader.attack(config, player.getVelocity(), getDisplayName());
      AttackInfo attackInfo = new AttackInfo(AttackType.FROSTED_TWIRL, this);

      if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
        player.getWorld().playSound(player.getLocation(), Sound.GLASS, 2, 1);
      }
    });
  }
}
