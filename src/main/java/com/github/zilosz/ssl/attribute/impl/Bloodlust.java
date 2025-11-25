package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Bloodlust extends PassiveAbility {

  @Override
  public String getUseType() {
    return "Melee";
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (event.getInfo().getType() != AttackType.MELEE) return;
    if (!RegenEvent.attempt(player, config.getDouble("Regen"))) return;

    event.getAttack().setName(getDisplayName());

    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 1, 2);
    Location loc = EntityUtils.center(event.getVictim());
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
    new ParticleMaker(particle).boom(SSL.getInstance(), loc, 3, 0.3, 7);
  }
}
