package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShadowAmbush extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(this.player), 1.5, 15, 0.5);

        EntityFinder finder = new EntityFinder(new DistanceSelector(this.config.getDouble("Range")));

        finder.findClosest(this.player).ifPresentOrElse(target -> {
            Location targetLoc = target.getLocation();
            Vector targetDir = targetLoc.getDirection().setY(0).normalize();
            Location spotBehind = targetLoc.subtract(targetDir).setDirection(targetDir);

            Block one = spotBehind.getBlock();
            Block two = spotBehind.clone().add(0, 1, 0).getBlock();

            this.player.teleport(one.getType().isSolid() || two.getType().isSolid() ? targetLoc : spotBehind);

            int duration = this.config.getInt("BlindnessDuration");
            new PotionEffectEvent(target, PotionEffectType.BLINDNESS, duration, 1).apply();

            this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 0.5f);
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(this.player), 2.5, 15, 0.5);

        }, () -> this.player.playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2f));
    }
}
