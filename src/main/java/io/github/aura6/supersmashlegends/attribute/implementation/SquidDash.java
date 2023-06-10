package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.DisguiseUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class SquidDash extends RightClickAbility {
    private Vector velocity;

    private BukkitTask dashTask;
    private int ticksDashing = -1;

    private BukkitTask invisibilityTask;
    private boolean invisible = false;

    private final Set<Item> particles = new HashSet<>();

    public SquidDash(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private int getMaxDashTicks() {
        return this.config.getInt("MaxTicks");
    }

    private void resetDash() {
        if (this.ticksDashing == -1) return;

        this.ticksDashing = -1;
        this.dashTask.cancel();

        DisguiseAPI.undisguiseToAll(this.player);
    }

    private void unHidePlayer() {
        this.invisible = false;

        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(this.player));
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(this.player), 1, 5, 0.5);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);
    }

    private void reset() {

        if (this.invisible) {
            this.unHidePlayer();
            this.invisibilityTask.cancel();
        }

        this.resetDash();

        this.particles.forEach(Item::remove);
        this.particles.clear();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
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
            Vector direction = VectorUtils.fromTo(this.player, target);
            Damage damageObj = Damage.Builder.fromConfig(this.config, direction).setDamage(damage).setKb(kb).build();
            this.plugin.getDamageManager().attemptAttributeDamage(target, damageObj, this);
        });

        this.invisible = true;
        Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(this.player));
        int ticks = (int) YamlReader.incLin(this.config, "InvisibilityTicks", this.ticksDashing, this.getMaxDashTicks());
        this.invisibilityTask = Bukkit.getScheduler().runTaskLater(this.plugin, this::unHidePlayer, ticks);

        this.resetDash();
        this.player.setVelocity(this.velocity);

        this.startCooldown();
    }

    private void startDash() {
        this.sendUseMessage();

        Vector direction =  this.player.getLocation().getDirection().setY(0);
        this.velocity = direction.multiply(this.config.getDouble("Velocity"));

        Disguise disguise = DisguiseUtils.applyDisguiseParams(this.player, new MobDisguise(DisguiseType.SQUID));
        DisguiseAPI.disguiseToAll(this.player, disguise);

        this.dashTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (++this.ticksDashing >= this.getMaxDashTicks()) {
                this.stopDash();
                return;
            }

            Location loc = this.player.getLocation();

            this.player.setVelocity(this.velocity);
            this.player.getWorld().playSound(loc, Sound.SPLASH2, 1, 1);

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

    @EventHandler
    public void onTakeAttributeDamage(AttributeDamageEvent event) {
        if (event.getVictim() != this.player) return;

        if (this.invisible) {
            this.unHidePlayer();
        }

        if (event.getAttribute() instanceof Melee) {

            if (this.ticksDashing > -1) {
                event.setCancelled(true);
            }

        } else {
            this.reset();
        }
    }

    @EventHandler
    public void onRegDamage(EntityDamageEvent event) {
        boolean isPlayer = event.getEntity() == this.player;
        boolean isDashing = this.ticksDashing > -1;
        boolean isMelee = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;

        if (isPlayer && isDashing && isMelee) {
            event.setCancelled(true);
        }
    }
}
