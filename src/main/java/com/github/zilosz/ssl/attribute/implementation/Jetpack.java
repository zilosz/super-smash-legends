package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class Jetpack extends PassiveAbility {

    public Jetpack(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

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
        Vector multiplier =  new Vector(velocity, 1, velocity);
        this.player.setVelocity(direction.multiply(multiplier).setY(this.config.getDouble("VelocityY")));

        Location location = this.player.getLocation();

        double particleX = location.getX();
        double particleY = location.getY() - this.config.getDouble("StreamFeetDistance");
        double particleZ = location.getZ();

        float spread = this.config.getFloat("MaxStreamSpread");

        while (spread > 0) {

            for (int i = 0; i < this.config.getDouble("ParticlesPerSpread") * spread; i++) {
                Location particleLoc = new Location(this.player.getWorld(), particleX, particleY, particleZ);
                new ParticleBuilder(EnumParticle.FLAME).setSpread(spread, 0, spread).show(particleLoc);
            }

            spread -= this.config.getDouble("StreamSpreadStep");
            particleY -= this.config.getDouble("StreamStep");
        }
    }

    @EventHandler
    public void onEnergy(EnergyEvent event) {
        if (event.getPlayer() == this.player && (this.player.isSneaking() || !EntityUtils.isPlayerGrounded(this.player))) {
            event.setEnergy(0);
        }
    }
}
