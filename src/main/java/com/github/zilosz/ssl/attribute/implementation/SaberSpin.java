package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.effect.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class SaberSpin extends ChargedRightClickAbility {
    private Vector direction;

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        this.direction = this.player.getEyeLocation().getDirection().setY(0);
    }

    @Override
    public void onChargeTick() {
        double radius = this.config.getDouble("Radius");

        Vector forward = this.direction.clone().multiply(radius);
        Location center = this.player.getLocation().add(forward).add(0, this.config.getDouble("Radius"), 0);

        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 0, 255));
        new ParticleMaker(particle).verticalRing(center, radius, 15);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 2, 1.3f);
        this.player.setVelocity(this.direction.clone().multiply(this.config.getDouble("ChargeSpeed")));

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player, center).forEach(target -> {
            if (SSL.getInstance().getDamageManager().attack(target, this, new Attack(this.config, this.direction))) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.BLAZE_BREATH, 2, 1);
            }
        });
    }
}
