package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
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

public class SaberSpin extends ChargedRightClickAbility {
    private Vector direction;

    public SaberSpin(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        direction = player.getEyeLocation().getDirection().setY(0);
    }

    @Override
    public void onChargeTick() {
        Vector forward = direction.clone().multiply(config.getDouble("Radius"));
        Location center = player.getLocation().add(forward).add(0, config.getDouble("Radius"), 0);

        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 0, 255).verticalRing(center, config.getDouble("Radius"), 15);
        player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 2, 1.3f);

        player.setVelocity(direction.clone().multiply(config.getDouble("ChargeSpeed")));

        EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

        new EntityFinder(plugin, selector).findAll(player, center).forEach(target -> {
            if (plugin.getDamageManager().attack(target, this, new AttackSettings(this.config, this.direction))) {
                player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2, 1);
            }
        });
    }
}
