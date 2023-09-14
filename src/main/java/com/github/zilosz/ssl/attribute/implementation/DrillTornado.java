package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class DrillTornado extends RightClickAbility {
    private BukkitTask prepareTask;
    private float pitch = 0.5f;
    private int ticksPreparing = 0;
    private boolean isDrilling = false;
    private BukkitTask drillTask;
    private BukkitTask drillCancelTask;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.ticksPreparing > 0 || this.isDrilling;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        int prepareDuration = this.config.getInt("PrepareTicks");

        this.prepareTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            this.player.setVelocity(new Vector(0, 0.03, 0));
            this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, this.pitch);

            if (this.ticksPreparing % 2 == 0) {
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK);
                new ParticleMaker(particle).hollowSphere(EntityUtils.center(this.player), 1, 20);
            }

            if (this.ticksPreparing++ < prepareDuration) {
                this.pitch += 1.5 / prepareDuration;
                return;
            }

            this.prepareTask.cancel();
            this.ticksPreparing = 0;
            this.isDrilling = true;

            this.drillTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                double velocity = this.config.getDouble("Velocity");
                this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity));

                this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

                for (double y = 0; y < 2 * Math.PI; y += this.config.getDouble("ParticleGap")) {
                    Location particleLoc = this.player.getLocation().add(1.5 * Math.cos(y), y, 1.5 * Math.sin(y));
                    new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).show(particleLoc);
                }

                EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

                new EntityFinder(selector).findAll(this.player).forEach(target -> {
                    Attack attack = YamlReader.attack(this.config, this.player.getVelocity(), this.getDisplayName());
                    AttackInfo attackInfo = new AttackInfo(AttackType.DRILL_TORNADO, this);

                    if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
                        this.player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 0.5f);
                    }
                });
            }, 0, 0);

            this.drillCancelTask = Bukkit.getScheduler()
                    .runTaskLater(SSL.getInstance(), () -> this.reset(true), this.config.getInt("Duration"));
        }, 0, 0);
    }

    private void reset(boolean natural) {
        this.pitch = 0.5f;
        this.ticksPreparing = 0;
        this.isDrilling = false;

        if (natural) {
            this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 1.5f);
            this.startCooldown();
        }

        if (this.prepareTask != null) {
            this.prepareTask.cancel();
        }

        if (this.drillTask != null) {
            this.drillTask.cancel();
            this.drillCancelTask.cancel();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onDamage(AttackEvent event) {
        if (event.getVictim() == this.player && this.ticksPreparing > 0) {
            this.reset(true);
        }
    }
}
