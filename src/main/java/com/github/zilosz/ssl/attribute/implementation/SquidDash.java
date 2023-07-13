package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.DisguiseUtils;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashSet;
import java.util.Set;

public class SquidDash extends RightClickAbility {
    private final Set<Item> particles = new HashSet<>();
    private Vector velocity;
    private BukkitTask dashTask;
    private int ticksDashing = -1;
    private BukkitTask invisibilityTask;
    private boolean invisible = false;

    private int getMaxDashTicks() {
        return this.config.getInt("MaxTicks");
    }

    private void stopDash() {
        Location center = EntityUtils.center(this.player);

        new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0)).solidSphere(center, 1.5, 7, 0.5);
        new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE).setOffset(0.6f, 0.6f, 0.6f).setLocation(center).display();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 2, 0.5f);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 1, 1);

        double damage = YamlReader.increasingValue(this.config, "Damage", this.ticksDashing, this.getMaxDashTicks());
        double kb = YamlReader.increasingValue(this.config, "Kb", this.ticksDashing, this.getMaxDashTicks());

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player).forEach(target -> {
            Vector direction = VectorUtils.fromTo(this.player, target);
            Attack attack = YamlReader.attack(this.config, direction, this.getDisplayName());
            attack.getDamage().setDamage(damage);
            attack.getKb().setKb(kb);

            AttackInfo attackInfo = new AttackInfo(AttackType.SQUID_DASH, this);
            SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
        });

        this.invisible = true;
        SSL.getInstance().getDamageManager().hideEntityIndicator(this.player);
        Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(this.player));

        int ticks = (int) YamlReader.increasingValue(
                this.config,
                "InvisibilityTicks",
                this.ticksDashing,
                this.getMaxDashTicks()
        );

        this.invisibilityTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), this::unHidePlayer, ticks);

        this.resetDash();
        this.player.setVelocity(this.velocity);

        this.startCooldown();
    }

    private void startDash() {
        this.sendUseMessage();

        Vector direction = this.player.getLocation().getDirection().setY(0).normalize();
        this.velocity = direction.multiply(this.config.getDouble("Velocity"));

        Disguise disguise = DisguiseUtils.applyDisguiseParams(this.player, new MobDisguise(DisguiseType.SQUID));
        DisguiseAPI.disguiseToAll(this.player, disguise);

        this.dashTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (++this.ticksDashing >= this.getMaxDashTicks()) {
                this.stopDash();
                return;
            }

            this.player.setVelocity(this.velocity);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH2, 1, 1);

            Section particleConfig = this.config.getSection("Particle");

            Location particleLoc = EntityUtils.center(this.player);
            particleLoc.setDirection(this.velocity.clone().normalize());

            double spread = particleConfig.getDouble("Spread");
            double speed = particleConfig.getDouble("Speed");

            int duration = particleConfig.getInt("Duration");

            for (int i = 0; i < particleConfig.getInt("CountPerTick"); i++) {
                Item particle = this.player.getWorld().dropItem(particleLoc, new ItemStack(Material.INK_SACK));
                particle.setPickupDelay(Integer.MAX_VALUE);

                particle.setVelocity(VectorUtils.randomVectorInDirection(particleLoc, spread).multiply(speed));

                Bukkit.getScheduler().runTaskLater(SSL.getInstance(), particle::remove, duration);
                this.particles.add(particle);
            }
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

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    private void reset() {

        if (this.invisible) {
            this.unHidePlayer();
        }

        this.resetDash();

        this.particles.forEach(Item::remove);
        this.particles.clear();
    }

    private void unHidePlayer() {
        this.invisibilityTask.cancel();
        this.invisible = false;

        SSL.getInstance().getDamageManager().showEntityIndicator(this.player);
        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(this.player));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);

        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
        new ParticleMaker(particle).solidSphere(EntityUtils.center(this.player), 1, 5, 0.5);
    }

    private void resetDash() {
        if (this.ticksDashing == -1) return;

        this.ticksDashing = -1;
        this.dashTask.cancel();

        DisguiseAPI.undisguiseToAll(this.player);
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (event.getVictim() == this.player && this.invisible) {
            this.unHidePlayer();
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getVictim() != this.player) return;

        if (event.getAttackInfo().getType() == AttackType.MELEE) {

            if (this.ticksDashing > -1) {
                event.setCancelled(true);
            }

        } else {

            if (this.ticksDashing > 0) {
                this.startCooldown();
            }

            this.reset();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.invisible) {
            event.getPlayer().hidePlayer(this.player);
        }
    }
}
