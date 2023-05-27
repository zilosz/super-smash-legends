package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class AerialAssault extends ChargedRightClickAbility {
    private Vector velocity;

    public AerialAssault(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        double speed = config.getDouble("Speed");
        double y = config.getDouble("VelocityY");
        velocity = player.getEyeLocation().getDirection().multiply(speed).setY(y);
    }

    @Override
    public void onChargeTick() {
        if (EntityUtils.isPlayerGrounded(player)) return;

        player.setVelocity(velocity);

        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 2);
        new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).ring(player.getLocation(), 1, 20);

        new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
            Damage damage = Damage.Builder.fromConfig(config, velocity).build();

            if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
            }
        });
    }
}
