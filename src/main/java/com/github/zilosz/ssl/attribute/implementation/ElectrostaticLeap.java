package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ElectrostaticLeap extends RightClickAbility {
    private BukkitTask task;

    public ElectrostaticLeap(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 3, 1);

        this.player.setVelocity(new Vector(0, this.config.getDouble("Velocity"), 0));
        this.kit.getJump().giveExtraJumps(1);

        for (double radius = 0.2; radius < this.config.getDouble("Radius"); radius += 0.25) {
            Location loc = this.player.getLocation();
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).ring(loc, 90, 0, radius, 20);
        }

        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (this.player.getVelocity().getY() <= 0) {
                this.task.cancel();
                return;
            }

            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(this.player.getLocation());
            HitBoxSelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

            new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {
                AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());

                if (this.plugin.getDamageManager().attack(target, this, settings)) {
                    this.player.playSound(this.player.getLocation(), Sound.ORB_PICKUP, 2, 1);
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
