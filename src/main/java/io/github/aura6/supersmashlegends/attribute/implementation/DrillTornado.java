package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;

public class DrillTornado extends ChargedRightClickAbility {

    public DrillTornado(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        player.setVelocity(player.getEyeLocation().getDirection().multiply(config.getDouble("Velocity")));
        player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 2, 1);

        for (double y = 0; y < 2 * Math.PI; y += config.getDouble("ParticleGap")) {
            Location particleLoc = player.getLocation().add(1.5 * Math.cos(y), y, 1.5 * Math.sin(y));
            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(particleLoc);
        }

        Damage damage = Damage.Builder.fromConfig(config, player.getLocation().getDirection()).build();

        new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
            if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 0.5f);
            }
        });
    }

    @Override
    public void onGeneralCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 1.5f);
    }
}
