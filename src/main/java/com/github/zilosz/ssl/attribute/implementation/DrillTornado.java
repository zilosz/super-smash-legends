package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.FloatingEntity;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class DrillTornado extends RightClickAbility {
    private BukkitTask prepareTask;
    private FloatingEntity<Player> floatingEntity;
    private float pitch = 0.5f;
    private int ticksPreparing = 0;
    private boolean isDrilling = false;
    private BukkitTask drillTask;
    private BukkitTask drillCancelTask;

    public DrillTornado(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.ticksPreparing > 0 || this.isDrilling;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        int prepareDuration = this.config.getInt("PrepareTicks");
        this.floatingEntity = FloatingEntity.fromEntity(this.player);

        this.prepareTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, this.pitch);

            if (this.ticksPreparing % 2 == 0) {
                new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).hollowSphere(EntityUtils.center(this.player), 1, 20);
            }

            if (this.ticksPreparing++ < prepareDuration) {
                this.pitch += 1.5 / prepareDuration;
                return;
            }

            this.prepareTask.cancel();
            this.ticksPreparing = 0;
            this.isDrilling = true;
            this.floatingEntity.destroy();

            this.drillTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                double velocity = this.config.getDouble("Velocity");
                this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity));

                this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

                for (double y = 0; y < 2 * Math.PI; y += this.config.getDouble("ParticleGap")) {
                    Location particleLoc = this.player.getLocation().add(1.5 * Math.cos(y), y, 1.5 * Math.sin(y));
                    new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(particleLoc);
                }

                EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

                new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {
                    AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());

                    if (this.plugin.getDamageManager().attack(target, this, settings)) {
                        this.player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 0.5f);
                    }
                });
            }, 0, 0);

            int duration = this.config.getInt("Duration");
            this.drillCancelTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.reset(true), duration);
        }, 0, 0);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
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
            this.floatingEntity.destroy();
        }

        if (this.drillTask != null) {
            this.drillTask.cancel();
            this.drillCancelTask.cancel();
        }
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (event.getVictim() == this.player && this.ticksPreparing > 0) {
            this.reset(true);
        }
    }
}