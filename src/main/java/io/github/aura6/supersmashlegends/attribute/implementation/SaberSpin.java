package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class SaberSpin extends ChargedRightClickAbility {
    private Vector direction;

    public SaberSpin(SuperSmashLegends plugin, Section config, Kit kit) {
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

        new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player, center).forEach(target -> {
            Damage damage = Damage.Builder.fromConfig(config, direction).build();

            if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2, 1);
            }
        });
    }
}
