package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class VoltTackle extends RightClickAbility {
    private int ticksMoving = -1;
    private BukkitTask moveTask;
    private final Map<Item, BukkitTask> particles = new HashMap<>();

    public VoltTackle(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private boolean isActive() {
        return this.ticksMoving > -1;
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.isActive();
    }

    private void reset(boolean cooldown, boolean sound) {
        this.moveTask.cancel();
        this.ticksMoving = -1;

        this.particles.forEach((item, task) -> {
            item.remove();
            task.cancel();
        });

        this.particles.clear();

        if (cooldown) {
            this.startCooldown();
        }

        if (sound) {
            this.player.playSound(this.player.getLocation(), Sound.WOLF_DEATH, 1, 0.5f);
        }
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1.5f);
        EntitySelector selector = new HitBoxSelector(this.config.getInt("HitBox"));

        int duration = this.config.getInt("DurationTicks");

        this.moveTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (++this.ticksMoving >= duration) {
                this.reset(true, true);
                return;
            }

            Location eyeLoc = this.player.getEyeLocation();
            double speed = YamlReader.incLin(this.config, "Velocity", this.ticksMoving, duration);
            Vector velocity = eyeLoc.getDirection().multiply(speed);

            if (Math.abs(velocity.getY()) > this.config.getDouble("MaxVelocityY")) {
                velocity.setY(Math.signum(velocity.getY()) * this.config.getDouble("MaxVelocityY"));
            }

            this.player.setVelocity(velocity);

            for (int i = 0; i < this.config.getInt("ParticlesPerTick"); i++) {
                Location center = EntityUtils.center(this.player);
                Item gold = this.player.getWorld().dropItem(center, new ItemStack(Material.GOLD_INGOT));
                gold.setPickupDelay(Integer.MAX_VALUE);
                gold.setVelocity(VectorUtils.randVector(null).multiply(this.config.getDouble("ParticleSpeed")));
                int particleDuration = this.config.getInt("ParticleDuration");
                this.particles.put(gold, Bukkit.getScheduler().runTaskLater(this.plugin, gold::remove, particleDuration));
            }

            float pitch = (float) MathUtils.increasingLinear(0.5, 2, duration, this.ticksMoving);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1, pitch);

            new EntityFinder(this.plugin, selector).findClosest(this.player).ifPresent(target -> {
                double damage = YamlReader.incLin(this.config, "Damage", this.ticksMoving, duration);
                double kb = YamlReader.incLin(this.config, "Kb", this.ticksMoving, duration);
                Damage damageObj = Damage.Builder.fromConfig(this.config, velocity).setDamage(damage).setKb(kb).build();

                if (this.plugin.getDamageManager().attemptAttributeDamage(target, damageObj, this)) {
                    this.player.getWorld().playSound(this.player.getLocation(), Sound.FALL_BIG, 1, 2);
                    this.player.getWorld().strikeLightningEffect(target.getLocation());

                    Section recoilConfig = this.config.getSection("Recoil");
                    double recoilDamage = YamlReader.incLin(recoilConfig, "Damage", this.ticksMoving, duration);
                    double recoilKb = YamlReader.incLin(recoilConfig, "Kb", this.ticksMoving, duration);

                    Damage recoilObj = Damage.Builder.fromConfig(recoilConfig, velocity.multiply(-1))
                            .setDamage(recoilDamage).setKb(recoilKb).build();

                    this.plugin.getDamageManager().attemptAttributeDamage(target, recoilObj, this);
                }

                this.reset(true, false);
            });
        }, 0, 0);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.isActive()) {
            this.reset(false, false);
        }
    }

    @EventHandler
    public void onDamage(AttributeDamageEvent event) {
        if (event.getVictim() == this.player && this.isActive()) {
            this.reset(true, true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == this.player && this.isActive()) {
            this.reset(true, true);
        }
    }
}