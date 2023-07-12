package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.CustomEvent;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Rasengan extends RightClickAbility {
    private BukkitTask mainTask;
    private BukkitTask cancelTask;
    private boolean leapt = false;
    private boolean active = false;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.active;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.active = true;
        this.hotbarItem.hide();

        this.start(this.player);
        Bukkit.getPluginManager().callEvent(new RasenganStartEvent(this));

        this.mainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            this.display(this.player);
            Bukkit.getPluginManager().callEvent(new RasenganDisplayEvent(this));
        }, 0, 0);

        int lifespan = this.config.getInt("Lifespan");
        this.cancelTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), this::reset, lifespan);
    }

    public void start(LivingEntity entity) {
        entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 0.5f, 1);
        int speed = this.config.getInt("Speed");
        new PotionEffectEvent(entity, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();
    }

    public void display(LivingEntity entity) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
        new ParticleMaker(particle).hollowSphere(EntityUtils.underHand(entity, 0), 0.15, 20);
    }

    private void reset() {
        if (!this.active) return;

        this.leapt = false;
        this.active = false;

        this.mainTask.cancel();
        this.cancelTask.cancel();

        this.hotbarItem.show();

        this.end(this.player);
        Bukkit.getPluginManager().callEvent(new RasenganEndEvent(this));

        this.startCooldown();
    }

    public void end(LivingEntity entity) {
        entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 2, 1);
        entity.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == this.player && event.getNewSlot() != this.slot && this.active) {
            this.reset();
        }
    }

    @EventHandler
    public void onMelee(AttackEvent event) {
        if (event.getAttribute().getPlayer() != this.player) return;
        if (!(event.getAttribute() instanceof Melee)) return;
        if (!this.active) return;

        modifyMeleeAttack(event.getAttack(), this.config);
        displayAttack(event.getVictim());

        this.reset();
    }

    public static void modifyMeleeAttack(Attack attack, Section config) {
        attack.getDamage().setDamage(config.getDouble("Damage"));
        attack.getKb().setKb(config.getDouble("Kb"));
        attack.getKb().setKbY(config.getDouble("KbY"));
        attack.getKb().setFactorsHealth(config.getBoolean("FactorsHealth"));
    }

    public static void displayAttack(LivingEntity victim) {
        victim.getWorld().playSound(victim.getLocation(), Sound.EXPLODE, 2, 1);

        for (int i = 0; i < 3; i++) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
            new ParticleMaker(particle).setSpread(0.4f).show(EntityUtils.center(victim));
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() != this.player) return;
        if (!this.active) return;
        if (this.leapt) return;

        this.leapt = true;

        this.leap(this.player);
        Bukkit.getPluginManager().callEvent(new RasenganLeapEvent(this));
    }

    public void leap(LivingEntity entity) {
        double velocity = this.config.getDouble("LeapVelocity");
        entity.setVelocity(entity.getEyeLocation().getDirection().multiply(velocity));
        entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_IDLE, 0.5f, 2);
    }

    @Getter
    public abstract static class RasenganEvent extends CustomEvent {
        private final Rasengan rasengan;

        public RasenganEvent(Rasengan rasengan) {
            this.rasengan = rasengan;
        }
    }

    public static class RasenganLeapEvent extends RasenganEvent {

        public RasenganLeapEvent(Rasengan rasengan) {
            super(rasengan);
        }
    }

    public static class RasenganEndEvent extends RasenganEvent {

        public RasenganEndEvent(Rasengan rasengan) {
            super(rasengan);
        }
    }

    public static class RasenganDisplayEvent extends RasenganEvent {

        public RasenganDisplayEvent(Rasengan rasengan) {
            super(rasengan);
        }
    }

    public static class RasenganStartEvent extends RasenganEvent {

        public RasenganStartEvent(Rasengan rasengan) {
            super(rasengan);
        }
    }
}
