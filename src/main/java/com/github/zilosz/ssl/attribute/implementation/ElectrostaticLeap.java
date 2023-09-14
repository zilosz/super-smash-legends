package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class ElectrostaticLeap extends RightClickAbility {
    private BukkitTask task;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 3, 1);

        this.player.setVelocity(new Vector(0, this.config.getDouble("Velocity"), 0));
        this.kit.getJump().giveExtraJumps(1);

        for (double radius = 0.2; radius < this.config.getDouble("Radius"); radius += 0.25) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 255, 0));
            new ParticleMaker(particle).ring(this.player.getLocation(), 90, 0, radius, 20);
        }

        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (this.player.getVelocity().getY() <= 0) {
                this.task.cancel();
                return;
            }

            new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).show(this.player.getLocation());
            HitBoxSelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

            new EntityFinder(selector).findAll(this.player).forEach(target -> {
                Vector direction = this.player.getLocation().getDirection();
                Attack attack = YamlReader.attack(this.config, direction, this.getDisplayName());
                AttackInfo attackInfo = new AttackInfo(AttackType.ELECTROSTATIC_LEAP, this);

                if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
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
