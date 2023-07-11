package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class GroundPound extends RightClickAbility {
    @Nullable private BukkitTask fallTask;
    @Nullable private BukkitTask checkAirborneTask;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.fallTask != null;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.VILLAGER_HAGGLE, 2, 1);

        this.player.setVelocity(new Vector(0, -this.config.getDouble("DownwardVelocity"), 0));
        double initialHeight = this.player.getLocation().getY();
        this.fallTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> this.onRun(initialHeight), 0, 0);

        if (this.checkAirborneTask == null) {
            this.checkAirborneTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), this::onGroundCheck, 0, 0);
        }
    }

    private void onRun(double initialHeight) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
        new ParticleMaker(particle).ring(EntityUtils.center(this.player), 90, 0, 1, 10);

        double fallen = Math.max(0, initialHeight - this.player.getLocation().getY());
        double maxFall = this.config.getDouble("MaxFall");
        double damage = YamlReader.increasingValue(this.config, "Damage", fallen, maxFall);
        double kb = YamlReader.increasingValue(this.config, "Kb", fallen, maxFall);

        boolean foundTarget = false;
        EntityFinder finder = new EntityFinder(new HitBoxSelector(this.config.getDouble("HitBox")));

        for (LivingEntity target : finder.findAll(this.player)) {
            Attack attack = YamlReader.attack(this.config, this.player.getLocation().getDirection());
            attack.getDamage().setDamage(damage);
            attack.getKb().setKb(kb);

            if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                foundTarget = true;

                this.player.getWorld().playSound(target.getLocation(), Sound.EXPLODE, 2, 2);
                new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(EntityUtils.top(target));
            }
        }

        if (foundTarget) {
            this.resetFall(false);
            this.kit.getJump().giveExtraJumps(1);

            double bounce = YamlReader.increasingValue(this.config, "Bounce", fallen, maxFall);
            this.player.setVelocity(new Vector(0, bounce, 0));
        }
    }

    private void onGroundCheck() {
        if (!EntityUtils.isPlayerGrounded(this.player)) return;

        if (this.fallTask != null) {
            this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_HIT, 2, 0.5f);
        }

        this.startCooldown();
        this.resetFall(true);
    }

    private void resetFall(boolean stopGroundTask) {

        if (this.fallTask != null) {
            this.fallTask.cancel();
        }

        this.fallTask = null;

        if (stopGroundTask && this.checkAirborneTask != null) {
            this.checkAirborneTask.cancel();
            this.checkAirborneTask = null;
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.resetFall(true);
    }

    @EventHandler
    public void onKb(AttributeKbEvent event) {
        if (event.getVictim() == this.player && this.fallTask != null) {
            event.getKb().setDirection(null);
        }
    }
}
