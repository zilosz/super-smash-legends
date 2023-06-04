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
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ElectrostaticLeap extends RightClickAbility {

    public ElectrostaticLeap(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 3, 1);

        player.setVelocity(new Vector(0, config.getDouble("Velocity"), 0));
        kit.getJump().giveExtraJumps(1);

        for (double radius = 0.2; radius < config.getDouble("Radius"); radius += 0.25) {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).ring(player.getLocation(), 90, 0, radius, 20);
        }

        ElectrostaticLeap instance = this;

        new BukkitRunnable() {

            @Override
            public void run() {

                if (player.getVelocity().getY() <= 0) {
                    cancel();
                    return;
                }

                new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(player.getLocation());

                new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
                    Damage damage = Damage.Builder.fromConfig(config, player.getLocation().getDirection()).build();

                    if (plugin.getDamageManager().attemptAttributeDamage(target, damage, instance)) {
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 1);
                    }
                });
            }

        }.runTaskTimer(plugin, 0, 0);
    }
}
