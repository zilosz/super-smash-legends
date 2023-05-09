package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class FrostedTwirl extends ChargedRightClickAbility {

    public FrostedTwirl(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        new ParticleBuilder(EnumParticle.SNOW_SHOVEL).ring(EntityUtils.center(player), 90, 0, 1, 20);
        player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 2, 1.5f);

        Vector forward = player.getEyeLocation().getDirection().multiply(config.getDouble("Velocity"));
        player.setVelocity(forward.setY(config.getDouble("VelocityY")));

        new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
            Damage damage = Damage.Builder.fromConfig(config, player.getLocation().getDirection()).build();

            if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                player.getWorld().playSound(player.getLocation(), Sound.GLASS, 2, 1);
            }
        });
    }
}
