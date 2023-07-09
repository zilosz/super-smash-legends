package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.utils.block.BlockRay;
import com.github.zilosz.ssl.utils.effect.ParticleMaker;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Teleport extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location loc = this.player.getEyeLocation();

        BlockRay blockRay = new BlockRay(loc, loc.getDirection());
        blockRay.cast(this.config.getInt("Range"));
        this.player.teleport(blockRay.getEmptyDestination());

        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);
        new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0)).solidSphere(loc, 1.1, 10, 0.3);
    }
}
