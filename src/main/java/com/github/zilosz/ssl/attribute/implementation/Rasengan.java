package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.KnockBack;
import com.github.zilosz.ssl.event.CustomEvent;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Rasengan extends RightClickAbility {
    @Nullable private BukkitTask task;
    private boolean leapt = false;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.task != null;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        RasenganStartEvent startEvent = new RasenganStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);
        RasenganStartEvent.apply(this.player, this.config.getInt("Speed"));

        this.hotbarItem.hide();

        this.task = new BukkitRunnable() {
            int ticksCharged = 0;

            @Override
            public void run() {

                if (++this.ticksCharged >= Rasengan.this.config.getInt("Lifespan")) {
                    Rasengan.this.reset();
                    return;
                }

                display(Rasengan.this.player);
            }
        }.runTaskTimer(SSL.getInstance(), 1, 0);
    }

    private void reset() {
        if (this.task == null) return;

        this.leapt = false;

        this.task.cancel();
        this.task = null;

        this.startCooldown();
        this.hotbarItem.show();

        end(this.player);
        this.player.removePotionEffect(PotionEffectType.SPEED);
    }

    public static void display(LivingEntity entity) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
        new ParticleMaker(particle).hollowSphere(EntityUtils.underHand(entity, 0), 0.15, 20);
    }

    public static void end(LivingEntity entity) {
        entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 2, 1);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == this.player && event.getNewSlot() != this.slot && this.task != null) {
            this.reset();
        }
    }

    @EventHandler
    public void onMelee(AttackEvent event) {
        if (this.task == null) return;
        if (event.getAttribute().getPlayer() != this.player) return;
        if (!(event.getAttribute() instanceof Melee)) return;

        this.reset();

        event.getAttack().getDamage().setDamage(this.config.getDouble("Damage"));

        KnockBack kb = event.getAttack().getKb();
        kb.setKb(this.config.getDouble("Kb"));
        kb.setKbY(this.config.getDouble("KbY"));

        event.getVictim().getWorld().playSound(event.getVictim().getLocation(), Sound.EXPLODE, 3, 1);
        displayAttackEffect(event.getVictim());
    }

    public static void displayAttackEffect(LivingEntity victim) {

        for (int i = 0; i < 3; i++) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
            new ParticleMaker(particle).setSpread(0.4f).show(EntityUtils.center(victim));
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (this.task == null || this.leapt) return;

        this.leapt = true;

        double velocity = this.config.getDouble("LeapVelocity");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_IDLE, 0.5f, 2);
    }

    public static class RasenganStartEvent extends CustomEvent {
        @Getter private final Rasengan rasengan;

        public RasenganStartEvent(Rasengan rasengan) {
            this.rasengan = rasengan;
        }

        public static void apply(LivingEntity entity, int speed) {
            entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 0.5f, 1);
            new PotionEffectEvent(entity, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();
        }
    }
}
