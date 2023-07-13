package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.utils.effects.Effects;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffectType;

public class StickySituation extends PassiveAbility {
    private boolean active = false;

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @Override
    public String getUseType() {
        return "Enter Low Health";
    }

    public void reset() {
        this.active = false;
        this.player.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onReceivingDamage(DamageEvent event) {
        if (event.getVictim() != this.player) return;
        if (this.active) return;
        if (event.willDie() || event.getNewHealth() > this.config.getDouble("HealthThreshold")) return;

        this.active = true;

        int speed = this.config.getInt("Speed");
        new PotionEffectEvent(this.player, PotionEffectType.SPEED, Integer.MAX_VALUE, speed);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 1);

        FireworkEffect.Builder settings = FireworkEffect.builder()
                .withColor(Color.fromRGB(0, 255, 0))
                .with(FireworkEffect.Type.BALL)
                .trail(true);

        Effects.launchFirework(this.player.getEyeLocation(), settings, 1);
    }

    @EventHandler
    public void onRegen(RegenEvent event) {
        if (event.getPlayer() != this.player) return;
        if (!this.active) return;
        if (this.player.getHealth() + event.getRegen() <= this.config.getDouble("HealthThreshold")) return;

        this.reset();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_DEATH, 1, 1);

        FireworkEffect.Builder settings = FireworkEffect.builder()
                .withColor(Color.fromRGB(0, 120, 0))
                .with(FireworkEffect.Type.BURST)
                .trail(true);

        Effects.launchFirework(this.player.getEyeLocation(), settings, 1);
    }

    @EventHandler
    public void onEnergy(EnergyEvent event) {
        if (event.getPlayer() == this.player && this.active) {
            event.setEnergy(event.getEnergy() * this.config.getFloat("EnergyMultiplier"));
        }
    }
}
