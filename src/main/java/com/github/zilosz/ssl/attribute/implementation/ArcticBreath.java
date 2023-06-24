package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
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

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location eyeLoc = this.player.getEyeLocation();

        int ringCount = this.config.getInt("RingCount");
        Vector step = eyeLoc.getDirection().multiply(this.config.getDouble("Range") / ringCount);

        double maxDamage = this.config.getDouble("MaxDamage");
        double damageDiff = maxDamage - this.config.getDouble("MinDamage");
        double damageStep = damageDiff / (ringCount - 1);

        double minRadius = this.config.getDouble("MinRadius");
        double radiusDiff = this.config.getDouble("MaxRadius") - minRadius;
        double radiusStep = radiusDiff / (ringCount - 1);

        this.createRing(eyeLoc, step, maxDamage, damageStep, minRadius, radiusStep, 0);
    }

    public void createRing(Location center, Vector step, double damage, double damageStep, double radius, double radiusStep, int ringCount) {
        if (ringCount == this.config.getInt("RingCount")) return;

        this.player.getWorld().playSound(center, Sound.GLASS, 2, 2);
        new ParticleBuilder(EnumParticle.SNOW_SHOVEL).ring(center, radius, this.config.getDouble("ParticleDensity"));

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player, center).forEach(target -> {
            AttackSettings settings = new AttackSettings(this.config, step).modifyDamage(dmg -> dmg.setDamage(damage));
            SSL.getInstance().getDamageManager().attack(target, this, settings);
        });

        Location next = center.add(step);
        this.createRing(next, step, damage - damageStep, damageStep, radius + radiusStep, radiusStep, ringCount + 1);
    }
}
