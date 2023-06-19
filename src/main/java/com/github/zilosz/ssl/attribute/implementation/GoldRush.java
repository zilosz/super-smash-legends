package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.event.attribute.JumpEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class GoldRush extends PassiveAbility {
    private List<Material> passableMaterials;
    private boolean isMining = false;
    private BukkitTask resetTask;
    private BukkitTask moveTask;
    private BukkitTask particleTask;

    public GoldRush(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Sneak";
    }

    @Override
    public void activate() {
        super.activate();

        this.passableMaterials = new ArrayList<>();
        this.config.getStringList("PassableMaterials").forEach(name -> this.passableMaterials.add(Material.valueOf(name)));
    }

    private void reset() {
        if (!this.isMining) return;

        this.isMining = false;

        this.resetTask.cancel();
        this.moveTask.cancel();
        this.particleTask.cancel();

        Location location = this.player.getLocation();

        while (!this.isPassable(location) && !this.isPassable(location.add(0, 1, 0))) {
            location.add(0, 1, 0);
        }

        this.player.getWorld().playSound(this.player.getLocation(), Sound.DIG_GRASS, 2, 0.5f);
        this.player.teleport(location);
        this.player.setGameMode(GameMode.SURVIVAL);
        this.player.removePotionEffect(PotionEffectType.BLINDNESS);

        double velocity = this.config.getDouble("EmergeVelocity");
        double velY = this.config.getDouble("EmergeVelocityY");
        this.player.setVelocity(location.getDirection().multiply(velocity).setY(velY));

        this.kit.getJump().replenish();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    private boolean isPassable(Location location) {
        return this.passableMaterials.contains(location.getBlock().getType());
    }

    private void teleport(Location location) {
        this.player.teleport(location.subtract(0, this.config.getDouble("Depth"), 0));
    }

    private void startMining() {
        this.isMining = true;

        this.player.getWorld().playSound(this.player.getLocation(), Sound.DIG_GRASS, 2, 0.85f);

        this.player.setExp(0);
        this.player.setGameMode(GameMode.SPECTATOR);
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10_000, this.config.getInt("Blindness")));
        this.teleport(this.player.getLocation());

        this.resetTask = Bukkit.getScheduler().runTaskLater(this.plugin, this::reset, this.config.getInt("MaxTicks"));

        this.moveTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            Location location = this.player.getLocation();
            Location body = location.clone().add(0, 1, 0);
            Location eyes = body.clone().add(0, 1, 0);
            Location aboveEyes = eyes.clone().add(0, 1, 0);

            if (!this.isPassable(eyes) && this.isPassable(aboveEyes)) {
                this.teleport(aboveEyes);

            } else if (this.isPassable(eyes) && this.isPassable(body) && !this.isPassable(location)) {
                this.teleport(body);

            } else if (this.isPassable(body) && this.isPassable(eyes)) {
                this.reset();
                return;
            }

            double velocity = this.config.getDouble("Velocity");
            this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity).setY(0));

            this.player.getWorld().playSound(this.player.getLocation(), Sound.DIG_STONE, 2, 1.5f);
        }, 0, 0);

        int ticksPerParticle = this.config.getInt("TicksPerParticle");

        this.particleTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            Location location = this.player.getLocation().add(0, 2, 0);

            while (!this.isPassable(location)) {
                location.add(0, 1, 0);
            }

            for (int i = 0; i < 5; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).setSpread(0.75f, 0, 0.75f).show(location);
            }
        }, ticksPerParticle, ticksPerParticle);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() != this.player) return;
        if (this.player.isSneaking()) return;

        if (this.isMining) {
            this.reset();
            return;
        }

        if (this.player.getExp() < 1) return;

        if (!EntityUtils.isPlayerGrounded(this.player)) {
            Chat.ABILITY.send(this.player, "&7You must be grounded to start mining.");
            return;
        }

        this.startMining();
    }

    @EventHandler
    public void onAbilityUse(AbilityUseEvent event) {
        if (event.getAbility().getPlayer() == this.player && this.isMining) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (event.getPlayer() == this.player && this.isMining) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnergy(EnergyEvent event) {
        if (event.getPlayer() == this.player && this.isMining) {
            event.setEnergy(0);
        }
    }
}
