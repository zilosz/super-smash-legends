package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SquidDash extends RightClickAbility {
    private Squid squid;
    private Vector velocity;
    private BukkitTask dashTask;
    private BukkitTask invisibilityTask;
    private boolean invisible = false;
    private int ticksDashing = -1;

    public SquidDash(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private int getMaxDashTicks() {
        return this.config.getInt("MaxTicks");
    }

    private void resetDash() {
        if (this.ticksDashing == -1) return;

        this.ticksDashing = -1;

        this.squid.remove();
        this.dashTask.cancel();
    }

    private void unHidePlayer() {
        this.invisible = false;

        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(this.player));
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(this.player), 1, 5, 0.5);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);
    }

    private void reset() {
        this.resetDash();

        if (this.invisible) {
            this.unHidePlayer();
            this.invisibilityTask.cancel();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    private void stopDash() {
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(player), 1.5, 7, 0.5);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 2, 0.5f);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 1, 1);

        double damage = YamlReader.incLin(this.config, "Damage", this.ticksDashing, this.getMaxDashTicks());
        double kb = YamlReader.incLin(this.config, "Kb", this.ticksDashing, this.getMaxDashTicks());

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));
        EntityFinder finder = new EntityFinder(this.plugin, selector).avoid(this.squid);

        finder.findAll(this.player).forEach(target -> {
            Vector direction = VectorUtils.fromTo(this.player, target);
            Damage damageObj = Damage.Builder.fromConfig(this.config, direction).setDamage(damage).setKb(kb).build();
            this.plugin.getDamageManager().attemptAttributeDamage(target, damageObj, this);
        });

        this.invisible = true;
        Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(this.player));
        int ticks = (int) YamlReader.incLin(this.config, "InvisibilityTicks", this.ticksDashing, this.getMaxDashTicks());
        this.invisibilityTask = Bukkit.getScheduler().runTaskLater(this.plugin, this::unHidePlayer, ticks);

        this.resetDash();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.player.setVelocity(this.velocity), 2);

        this.startCooldown();
    }

    private void startDash() {
        this.sendUseMessage();

        Vector direction =  this.player.getLocation().getDirection().setY(0);
        this.velocity = direction.multiply(this.config.getDouble("Velocity"));

        this.squid = this.player.getWorld().spawn(this.player.getLocation().setDirection(direction), Squid.class);
        this.squid.setPassenger(this.player);

        this.dashTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (++this.ticksDashing >= this.getMaxDashTicks()) {
                this.stopDash();
                return;
            }

            this.squid.setVelocity(this.velocity);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH2, 1, 1);
        }, 0, 0);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        if (this.ticksDashing == -1) {
            this.startDash();
        } else {
            this.stopDash();
        }
    }

    @EventHandler
    public void onTakeAttributeDamage(AttributeDamageEvent event) {
        if (event.getVictim() != this.player) return;

        if (this.invisible) {
            this.unHidePlayer();
        }

        if (event.getAttribute() instanceof Melee) {
            event.setCancelled(this.ticksDashing > -1);

        } else {
            this.reset();
        }
    }

    @EventHandler
    public void onSquidDamage(DamageEvent event) {
        if (event.getVictim() == this.squid) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRegDamage(EntityDamageEvent event) {
        if (event.getEntity() == this.squid || event.getEntity() == this.player && this.ticksDashing > -1 && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDismount(VehicleExitEvent event) {
        if (event.getExited() == this.squid) {
            event.setCancelled(true);
        }
    }
}
