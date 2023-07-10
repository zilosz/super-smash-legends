package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Jetpack extends PassiveAbility {

    @Override
    public String getUseType() {
        return "Sneak";
    }

    @Override
    public void run() {
        if (!this.player.isSneaking() || this.player.getExp() < this.config.getFloat("EnergyPerTick")) return;

        this.player.setExp(this.player.getExp() - this.config.getFloat("EnergyPerTick"));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 2, 2);

        Vector direction = this.player.getEyeLocation().getDirection();
        double velocity = this.config.getDouble("Velocity");
        Vector multiplier = new Vector(velocity, 1, velocity);
        this.player.setVelocity(direction.multiply(multiplier).setY(this.config.getDouble("VelocityY")));

        Location location = this.player.getLocation();

        double particleX = location.getX();
        double particleY = location.getY() - this.config.getDouble("StreamFeetDistance");
        double particleZ = location.getZ();

        float spread = this.config.getFloat("MaxStreamSpread");

        while (spread > 0) {

            for (int i = 0; i < this.config.getDouble("ParticlesPerSpread") * spread; i++) {
                Location loc = new Location(this.player.getWorld(), particleX, particleY, particleZ);
                new ParticleMaker(new ParticleBuilder(ParticleEffect.FLAME)).setSpread(spread, 0, spread).show(loc);
            }

            spread -= this.config.getDouble("StreamSpreadStep");
            particleY -= this.config.getDouble("StreamStep");
        }
    }

    @EventHandler
    public void onEnergy(EnergyEvent event) {
        if (event.getPlayer() != this.player) return;

        if (this.player.isSneaking() || !EntityUtils.isPlayerGrounded(this.player)) {
            event.setEnergy(0);
        }
    }
}
