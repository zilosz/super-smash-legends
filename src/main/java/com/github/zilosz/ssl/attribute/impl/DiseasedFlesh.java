package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.util.collection.RandomCollection;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class DiseasedFlesh extends RightClickAbility {
  private RandomCollection<ItemStack> items;

  @Override
  public void activate() {
    super.activate();
    items = new RandomCollection<>();

    for (Section section : YamlReader.sections(config.getSection("Items"))) {
      items.add(YamlReader.stack(section), section.getDouble("Weight"));
    }
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    launch(true);

    for (int i = 1; i < config.getInt("Count"); i++) {
      launch(false);
    }

    player.getWorld().playSound(player.getEyeLocation(), Sound.ZOMBIE_PIG_HURT, 1, 2);

  }

  private void launch(boolean first) {
    AttackInfo attackInfo = new AttackInfo(AttackType.DISEASED_FLESH, this);
    FleshProjectile projectile = new FleshProjectile(config, attackInfo);
    projectile.setItemStack(items.next());

    if (first) {
      projectile.setSpread(0);
    }

    projectile.launch();
  }

  private static class FleshProjectile extends ItemProjectile {

    public FleshProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      new PotionEffectEvent(target, PotionEffectType.WITHER, 20, 1).apply();
    }

    @Override
    public void onTick() {
      ParticleBuilder particle =
          new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(162, 42, 42));
      new ParticleMaker(particle).show(entity.getLocation());
    }
  }
}
