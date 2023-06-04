package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class ArcticBreath extends RightClickAbility {

    public ArcticBreath(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location eyeLoc = player.getEyeLocation();
        Vector step = eyeLoc.getDirection().multiply(config.getDouble("Range") / config.getInt("RingCount"));

        double damageStep = (config.getDouble("MaxDamage") - config.getDouble("MinDamage")) / (config.getInt("RingCount") - 1);
        double radiusStep = (config.getDouble("MaxRadius") - config.getDouble("MinRadius")) / (config.getInt("RingCount") - 1);

        createRing(eyeLoc, step, config.getDouble("MaxDamage"), damageStep, config.getDouble("MinRadius"), radiusStep, 0);
    }

    public void createRing(Location center, Vector step, double damage, double damageStep, double radius, double radiusStep, int ringCount) {
        if (ringCount == config.getInt("RingCount")) return;

        player.getWorld().playSound(center, Sound.GLASS, 2, 2);
        new ParticleBuilder(EnumParticle.SNOW_SHOVEL).ring(center, radius, config.getDouble("ParticleDensity"));

        new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player, center).forEach(target -> {
            Damage dmg = Damage.Builder.fromConfig(config, step).setDamage(damage).build();
            plugin.getDamageManager().attemptAttributeDamage(target, dmg, this);
        });

        createRing(center.add(step), step, damage - damageStep, damageStep, radius + radiusStep, radiusStep, ringCount + 1);
    }
}
