package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.event.EnergyEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class Jetpack extends PassiveAbility {

    public Jetpack(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Sneak";
    }

    @Override
    public void run() {
        if (!player.isSneaking() || player.getExp() < config.getFloat("EnergyPerTick")) return;

        player.setExp(player.getExp() - config.getFloat("EnergyPerTick"));

        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 2, 2);

        Vector direction = player.getEyeLocation().getDirection();
        double velocity = config.getDouble("Velocity");
        player.setVelocity(direction.multiply(new Vector(velocity, 1, velocity)).setY(config.getDouble("VelocityY")));

        Location location = player.getLocation();

        double particleX = location.getX();
        double particleY = location.getY() - config.getDouble("StreamFeetDistance");
        double particleZ = location.getZ();

        double stepped = 0;
        float spread = config.getFloat("MaxStreamSpread");

        while (stepped < config.getDouble("StreamLength")) {

            for (int i = 0; i < (int) Math.ceil(config.getDouble("ParticlesPerStep") * spread); i++) {
                Location particleLoc = new Location(player.getWorld(), particleX, particleY, particleZ);
                new ParticleBuilder(EnumParticle.FLAME).setSpread(spread, 0, spread).show(particleLoc);
            }

            spread -= config.getDouble("StreamSpreadStep");
            particleY -= config.getDouble("StreamStep");
            stepped += config.getDouble("StreamStep");
        }
    }

    @EventHandler
    public void onEnergy(EnergyEvent event) {
        if (event.getPlayer() == player && (player.isSneaking() || !EntityUtils.isPlayerGrounded(player))) {
            event.setEnergy(0);
        }
    }
}
