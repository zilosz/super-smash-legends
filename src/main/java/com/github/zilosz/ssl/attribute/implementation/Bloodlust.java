package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
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
        if (event.getAttackInfo().getAttribute().getPlayer() != this.player) return;
        if (event.getAttackInfo().getType() != AttackType.MELEE) return;
        if (!RegenEvent.attempt(this.player, this.config.getDouble("Regen"))) return;

        event.getAttack().setName(this.getDisplayName());

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_UNFECT, 1, 2);
        Location loc = EntityUtils.center(event.getVictim());
        new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).boom(SSL.getInstance(), loc, 3, 0.3, 7);
    }
}
