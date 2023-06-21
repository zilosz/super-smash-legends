package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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
    private final Map<Item, BukkitTask> particles = new HashMap<>();
    private int ticksMoving = -1;
    private BukkitTask moveTask;

    public VoltTackle(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.ticksMoving > -1;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1.5f);
        EntitySelector selector = new HitBoxSelector(this.config.getInt("HitBox"));

        int duration = this.config.getInt("DurationTicks");

        this.moveTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            ++this.ticksMoving;
            if (this.ticksMoving >= duration) {
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

                this.particles.put(
                        gold,
                        Bukkit.getScheduler().runTaskLater(this.plugin, gold::remove, particleDuration)
                );
            }

            float pitch = (float) MathUtils.increasingLinear(0.5, 2, duration, this.ticksMoving);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1, pitch);

            new EntityFinder(this.plugin, selector).findClosest(this.player).ifPresent(target -> {
                double damage = YamlReader.incLin(this.config, "Damage", this.ticksMoving, duration);
                double kb = YamlReader.incLin(this.config, "Kb", this.ticksMoving, duration);

                AttackSettings settings = new AttackSettings(this.config, velocity)
                        .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                        .modifyKb(kbSettings -> kbSettings.setKb(kb));

                if (this.plugin.getDamageManager().attack(target, this, settings)) {
                    this.player.getWorld().playSound(this.player.getLocation(), Sound.FALL_BIG, 1, 2);
                    this.player.getWorld().strikeLightningEffect(target.getLocation());

                    Section recoilConfig = this.config.getSection("Recoil");
                    double recoilDamage = YamlReader.incLin(recoilConfig, "Damage", this.ticksMoving, duration);
                    double recoilKb = YamlReader.incLin(recoilConfig, "Kb", this.ticksMoving, duration);

                    AttackSettings recoil = new AttackSettings(recoilConfig, velocity.multiply(-1)).modifyDamage(
                                    damageSettings -> damageSettings.setDamage(recoilDamage))
                            .modifyKb(kbSettings -> kbSettings.setKb(recoilKb));

                    this.plugin.getDamageManager().attack(target, this, recoil);
                }

                this.reset(true, false);
            });
        }, 0, 0);
    }

    private void reset(boolean cooldown, boolean sound) {
        if (this.ticksMoving == -1) return;

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
    public void deactivate() {
        this.reset(false, false);
        super.deactivate();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == this.player && !this.player.isSneaking()) {
            this.reset(true, true);
        }
    }
}
