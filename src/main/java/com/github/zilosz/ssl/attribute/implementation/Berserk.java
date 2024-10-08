package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Berserk extends RightClickAbility {
    private boolean active = false;
    private BukkitTask resetTask;
    private Firework firework;
    private BukkitTask particleTask;
    private int ogJumps;

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
        new PotionEffectEvent(this.player, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();

        FireworkEffect.Builder settings = FireworkEffect.builder()
                .withColor(this.kit.getColor().getBukkitColor())
                .with(FireworkEffect.Type.BURST)
                .trail(true);

        this.firework = Effects.launchFirework(this.player.getLocation(), settings, 1);

        this.particleTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            Location loc = this.player.getLocation().add(0, 0.3, 0);
            new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).ring(loc, 90, 0, 0.5, 20);
        }, 0, 5);

        this.player.playSound(this.player.getLocation(), Sound.WOLF_GROWL, 1, 1);

        this.resetTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
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
    public void onAttack(AttackEvent event) {
        if (event.getAttackInfo().getAttribute().getPlayer() != this.player) return;
        if (!this.active) return;

        double multiplier = this.config.getDouble("DamageMultiplier");
        event.getDamage().setDamage(event.getDamage().getDamage() * multiplier);

        if (event.getAttackInfo().getType() == AttackType.MELEE) {
            event.getAttack().setName(this.getDisplayName());
        }
    }
}
