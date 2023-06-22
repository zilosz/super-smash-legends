package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.entity.DisguiseUtils;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.minecraft.server.v1_8_R3.EnumParticle;
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

import java.util.HashSet;
import java.util.Set;

public class SquidDash extends RightClickAbility {
    private final Set<Item> particles = new HashSet<>();
    private Vector velocity;
    private BukkitTask dashTask;
    private int ticksDashing = -1;
    private BukkitTask invisibilityTask;
    private boolean invisible = false;

    public SquidDash(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private int getMaxDashTicks() {
        return this.config.getInt("MaxTicks");
    }

    private void stopDash() {
        Location center = EntityUtils.center(this.player);
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(center, 1.5, 7, 0.5);
        new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).setSpread(0.6f, 0.6f, 0.6f).show(center);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 2, 0.5f);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 1, 1);

        double damage = YamlReader.incLin(this.config, "Damage", this.ticksDashing, this.getMaxDashTicks());
        double kb = YamlReader.incLin(this.config, "Kb", this.ticksDashing, this.getMaxDashTicks());

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {

            AttackSettings settings = new AttackSettings(this.config, VectorUtils.fromTo(this.player, target))
                    .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                    .modifyKb(kbSettings -> kbSettings.setKb(kb));

            this.plugin.getDamageManager().attack(target, this, settings);
        });

        this.invisible = true;
        this.plugin.getDamageManager().hideEntityIndicator(this.player);
        Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(this.player));

        int ticks = (int) YamlReader.incLin(
                this.config,
                "InvisibilityTicks",
                this.ticksDashing,
                this.getMaxDashTicks()
        );

        this.invisibilityTask = Bukkit.getScheduler().runTaskLater(this.plugin, this::unHidePlayer, ticks);

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

        this.dashTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

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

                particle.setVelocity(VectorUtils.getRandomVectorInDirection(particleLoc, spread).multiply(speed));

                Bukkit.getScheduler().runTaskLater(this.plugin, particle::remove, duration);
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
        this.plugin.getDamageManager().showEntityIndicator(this.player);
        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(this.player));
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(this.player), 1, 5, 0.5);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);
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
    public void onAttributeDamage(AttributeDamageEvent event) {
        if (event.getVictim() != this.player) return;

        if (event.getAttribute() instanceof Melee) {

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
