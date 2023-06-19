package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class ArcticBreath extends RightClickAbility {

    public ArcticBreath(SSL plugin, Section config, Kit kit) {
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

        EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

        new EntityFinder(plugin, selector).findAll(player, center).forEach(target -> {
            AttackSettings settings = new AttackSettings(this.config, step).modifyDamage(dmg -> dmg.setDamage(damage));
            plugin.getDamageManager().attack(target, this, settings);
        });

        createRing(center.add(step), step, damage - damageStep, damageStep, radius + radiusStep, radiusStep, ringCount + 1);
    }
}
