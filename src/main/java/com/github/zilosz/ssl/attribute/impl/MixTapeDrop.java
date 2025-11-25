package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;

public class MixTapeDrop extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    AttackInfo attackInfo = new AttackInfo(AttackType.MIX_TAPE_DROP, this);
    new MixTapeProjectile(config.getSection("Projectile"), attackInfo).launch();
    player.getWorld().playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 1);

    player.setVelocity(player
        .getEyeLocation()
        .getDirection()
        .multiply(-config.getDouble("Recoil")));
  }

  private static class MixTapeProjectile extends ItemProjectile {

    public MixTapeProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      showEffect();

      EntitySelector selector = new DistanceSelector(config.getDouble("Ground.Radius"));
      EntityFinder finder = new EntityFinder(selector);

      finder.findAll(launcher, entity.getLocation()).forEach(target -> {
        Vector direction = VectorUtils.fromTo(entity, target);
        String name = ((Ability) attackInfo.getAttribute()).getDisplayName();
        Attack attack = YamlReader.attack(config.getSection("Ground"), direction, name);

        if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
          launcher.playSound(launcher.getLocation(), Sound.NOTE_PLING, 2, 2);
        }
      });
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      showEffect();
    }

    @Override
    public void onTick() {
      NoteColor note = new NoteColor((int) MathUtils.randRange(0, 25));
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.NOTE).setParticleData(note);
      new ParticleMaker(particle).show(entity.getLocation());
    }

    private void showEffect() {
      entity.getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 2, 1);
      new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(entity.getLocation());
    }
  }
}
