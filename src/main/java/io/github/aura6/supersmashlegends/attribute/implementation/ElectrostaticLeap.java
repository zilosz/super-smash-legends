package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ElectrostaticLeap extends RightClickAbility {
    private BukkitTask task;

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

        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (this.player.getVelocity().getY() <= 0) {
                this.task.cancel();
                return;
            }

            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(player.getLocation());

            new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
                AttackSettings settings = new AttackSettings(this.config, player.getLocation().getDirection());

                if (plugin.getDamageManager().attack(target, this, settings)) {
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 1);
                }
            });
        }, 0, 0);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.task != null) {
            this.task.cancel();
        }
    }
}
