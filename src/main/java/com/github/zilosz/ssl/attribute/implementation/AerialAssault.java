package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.effect.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class AerialAssault extends ChargedRightClickAbility {
    private Vector velocity;

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        double speed = this.config.getDouble("Speed");
        double y = this.config.getDouble("VelocityY");
        this.velocity = this.player.getEyeLocation().getDirection().multiply(speed).setY(y);
    }

    @Override
    public void onChargeTick() {
        if (EntityUtils.isPlayerGrounded(this.player)) return;

        this.player.setVelocity(this.velocity);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 2);

        Vector forward = this.velocity.clone().normalize().multiply(2);
        Location particleCenter = EntityUtils.center(this.player).setDirection(this.velocity).add(forward);
        new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).ring(particleCenter, 1.5, 20);

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player).forEach(target -> {
            Attack attack = new Attack(this.config, this.velocity);

            if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
            }
        });
    }
}
