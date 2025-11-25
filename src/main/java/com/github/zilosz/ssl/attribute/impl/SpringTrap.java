package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class SpringTrap extends RightClickAbility {
  private int uses;

  @Override
  public void onClick(PlayerInteractEvent event) {
    AttackInfo attackInfo = new AttackInfo(AttackType.SPRING_TRAP, this);
    SpringProjectile springProjectile = new SpringProjectile(config, attackInfo);
    springProjectile.setOverrideLocation(player.getLocation().setDirection(new Vector(0, -1, 0)));
    springProjectile.launch();

    double velocity = config.getDouble("ForwardVelocity");
    double velocityY = config.getDouble("ForwardVelocityY");
    player.setVelocity(player.getEyeLocation().getDirection().multiply(velocity).setY(velocityY));

    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2, 2);

    if (++uses >= config.getInt("Uses")) {
      startCooldown();
      uses = 0;
    }
  }

  private static class SpringProjectile extends BlockProjectile {

    public SpringProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      displayEffect();

      EntityFinder finder = new EntityFinder(new DistanceSelector(config.getDouble("Radius")));

      finder.findAll(launcher, entity.getLocation()).forEach(target -> {
        Vector direction = VectorUtils.fromTo(entity, target);
        String name = ((Ability) attackInfo.getAttribute()).getDisplayName();
        Attack attack = YamlReader.attack(config.getSection("Aoe"), direction, name);
        SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
      });
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      displayEffect();
    }

    private void displayEffect() {
      Location loc = entity.getLocation();
      double radius = config.getDouble("Radius");

      for (int i = 0; i < 2; i++) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
        new ParticleMaker(particle).solidSphere(loc, radius, 5, 0.5);
      }

      entity.getWorld().playSound(loc, Sound.ZOMBIE_WOODBREAK, 2, 1);
    }
  }
}
