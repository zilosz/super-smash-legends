package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
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

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.ticksMoving > -1;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1.5f);
        EntitySelector selector = new HitBoxSelector(this.config.getInt("HitBox"));
        int duration = this.config.getInt("DurationTicks");

        this.moveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (++this.ticksMoving >= duration) {
                this.reset();
                this.startCooldown();
                this.playEndSound();
                return;
            }

            Location eyeLoc = this.player.getEyeLocation();
            double speed = YamlReader.increasingValue(this.config, "Velocity", this.ticksMoving, duration);
            Vector velocity = eyeLoc.getDirection().multiply(speed);

            if (Math.abs(velocity.getY()) > this.config.getDouble("MaxVelocityY")) {
                velocity.setY(Math.signum(velocity.getY()) * this.config.getDouble("MaxVelocityY"));
            }

            this.player.setVelocity(velocity);

            for (int i = 0; i < this.config.getInt("ParticlesPerTick"); i++) {
                Location center = EntityUtils.center(this.player);
                Item gold = this.player.getWorld().dropItem(center, new ItemStack(Material.GOLD_INGOT));
                gold.setPickupDelay(Integer.MAX_VALUE);
                gold.setVelocity(VectorUtils.randomVector(null).multiply(this.config.getDouble("ParticleSpeed")));

                int particleDuration = this.config.getInt("ParticleDuration");

                this.particles.put(gold, Bukkit.getScheduler()
                        .runTaskLater(SSL.getInstance(), gold::remove, particleDuration));
            }

            float pitch = (float) MathUtils.increasingValue(0.5, 2, duration, this.ticksMoving);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1, pitch);

            new EntityFinder(selector).findClosest(this.player).ifPresent(target -> {
                double damage = YamlReader.increasingValue(this.config, "Damage", this.ticksMoving, duration);
                double kb = YamlReader.increasingValue(this.config, "Kb", this.ticksMoving, duration);

                Attack attack = YamlReader.attack(this.config, velocity, this.getDisplayName());
                attack.getDamage().setDamage(damage);
                attack.getKb().setKb(kb);

                AttackInfo attackInfo = new AttackInfo(AttackType.VOLT_TACKLE, this);

                if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
                    this.player.getWorld().playSound(this.player.getLocation(), Sound.FALL_BIG, 1, 2);
                    this.player.getWorld().strikeLightningEffect(target.getLocation());

                    Section settings = this.config.getSection("Recoil");
                    double recoilDamage = YamlReader.increasingValue(settings, "Damage", this.ticksMoving, duration);
                    double recoilKb = YamlReader.increasingValue(settings, "Kb", this.ticksMoving, duration);

                    Attack recoil = YamlReader.attack(settings, velocity.multiply(-1), this.getDisplayName());
                    recoil.getDamage().setDamage(recoilDamage);
                    recoil.getKb().setKb(recoilKb);

                    AttackInfo info = new AttackInfo(AttackType.VOLT_TACKLE_RECOIL, this);
                    SSL.getInstance().getDamageManager().attack(this.player, recoil, info);
                }

                this.reset();
                this.startCooldown();
            });
        }, 0, 0);
    }

    private void reset() {
        if (this.ticksMoving == -1) return;

        this.moveTask.cancel();
        this.ticksMoving = -1;

        CollectionUtils.removeWhileIteratingOverEntry(this.particles, Item::remove, BukkitTask::cancel);
    }

    private void playEndSound() {
        this.player.playSound(this.player.getLocation(), Sound.WOLF_DEATH, 1, 0.5f);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == this.player && !this.player.isSneaking()) {
            this.reset();
            this.startCooldown();
            this.playEndSound();
        }
    }
}
