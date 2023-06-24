package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
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

public class SaberSpin extends ChargedRightClickAbility {
    private Vector direction;

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        this.direction = this.player.getEyeLocation().getDirection().setY(0);
    }

    @Override
    public void onChargeTick() {
        Vector forward = this.direction.clone().multiply(this.config.getDouble("Radius"));
        Location center = this.player.getLocation().add(forward).add(0, this.config.getDouble("Radius"), 0);

        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 0, 255)
                .verticalRing(center, this.config.getDouble("Radius"), 15);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 2, 1.3f);

        this.player.setVelocity(this.direction.clone().multiply(this.config.getDouble("ChargeSpeed")));

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player, center).forEach(target -> {
            if (SSL.getInstance().getDamageManager().attack(target, this, new AttackSettings(this.config, this.direction))) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.BLAZE_BREATH, 2, 1);
            }
        });
    }
}
