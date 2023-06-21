package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.Effects;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Berserk extends RightClickAbility {
    private boolean active = false;
    private BukkitTask resetTask;
    private Firework firework;
    private BukkitTask particleTask;
    private int ogJumps;

    public Berserk(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.active;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.active = true;

        this.ogJumps = this.kit.getJump().getMaxCount();
        this.kit.getJump().setMaxCount(this.ogJumps + this.config.getInt("ExtraJumps"));

        int speed = this.config.getInt("Speed");
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speed));

        this.firework = Effects.launchFirework(this.player.getLocation(), Color.RED, 1);
        ParticleBuilder particle = new ParticleBuilder(EnumParticle.REDSTONE);

        this.particleTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            particle.ring(this.player.getLocation().add(0, 0.3, 0), 90, 0, 0.5, 20);
        }, 0, 5);

        this.player.playSound(this.player.getLocation(), Sound.WOLF_GROWL, 1, 1);

        this.resetTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.reset();
            this.startCooldown();
        }, this.config.getInt("Duration"));
    }

    public void reset() {
        if (!this.active) return;

        this.active = false;

        this.kit.getJump().setMaxCount(this.ogJumps);
        this.player.removePotionEffect(PotionEffectType.SPEED);

        this.firework.remove();
        this.particleTask.cancel();
        this.resetTask.cancel();

        this.player.playSound(this.player.getLocation(), Sound.WOLF_WHINE, 1, 1);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onDamage(AttributeDamageEvent event) {
        if (this.active && event.getAttribute().getPlayer() == this.player) {
            double multiplier = this.config.getDouble("DamageMultiplier");
            event.getDamageSettings().setDamage(event.getDamageSettings().getDamage() * multiplier);
        }
    }
}