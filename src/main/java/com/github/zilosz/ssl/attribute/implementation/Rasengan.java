package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.damage.DamageSettings;
import com.github.zilosz.ssl.damage.KbSettings;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.CustomEvent;
import com.github.zilosz.ssl.kit.Kit;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Rasengan extends RightClickAbility {
    private BukkitTask task;
    private boolean leapt = false;

    public Rasengan(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || task != null;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        RasenganStartEvent startEvent = new RasenganStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);
        startEvent.apply(player, config.getInt("Speed"));

        this.hotbarItem.hide();

        task = new BukkitRunnable() {
            int ticksCharged = 0;

            @Override
            public void run() {

                if (++ticksCharged >= config.getInt("Lifespan")) {
                    reset();
                    return;
                }

                display(player);
            }
        }.runTaskTimer(plugin, 1, 0);
    }

    public static void display(LivingEntity entity) {
        Location location = EntityUtils.underHand(entity, 0);
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(173, 216, 230).hollowSphere(location, 0.15, 20);
    }

    public static void end(LivingEntity entity) {
        entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 2, 1);
    }

    private void reset() {
        if (this.task == null) return;

        this.leapt = false;

        task.cancel();
        task = null;

        this.startCooldown();
        this.hotbarItem.show();

        end(player);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == player && event.getNewSlot() != slot && task != null) {
            reset();
        }
    }

    public static void displayAttackEffect(LivingEntity victim) {
        Location loc = EntityUtils.center(victim);

        for (int i = 0; i < 3; i++) {
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).setSpread(0.4f, 0.4f, 0.4f).show(loc);
        }
    }

    @EventHandler
    public void onMelee(AttackEvent event) {
        if (this.task == null) return;
        if (event.getAttribute().getPlayer() != player) return;
        if (!(event.getAttribute() instanceof Melee)) return;

        reset();

        DamageSettings damageSettings = event.getAttackSettings().getDamageSettings();
        damageSettings.setDamage(this.config.getDouble("Damage"));

        KbSettings kbSettings = event.getAttackSettings().getKbSettings();
        kbSettings.setKb(this.config.getDouble("Kb"));
        kbSettings.setKbY(this.config.getDouble("KbY"));

        event.getVictim().getWorld().playSound(event.getVictim().getLocation(), Sound.EXPLODE, 3, 1);
        displayAttackEffect(event.getVictim());
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

        public void apply(LivingEntity entity, int speed) {
            entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 0.5f, 1);
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speed));
        }
    }
}
