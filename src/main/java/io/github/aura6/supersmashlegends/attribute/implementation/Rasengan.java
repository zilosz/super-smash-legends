package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.CustomEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Rasengan extends RightClickAbility {
    private BukkitTask task;

    public Rasengan(SuperSmashLegends plugin, Section config, Kit kit) {
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

        hotbarItem.hide();

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
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(173, 216, 230).solidSphere(location, 0, 50, 0.05);
    }

    public static void end(LivingEntity entity) {
        entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 2, 1);
    }

    public boolean isActive() {
        return task != null;
    }

    private void reset() {
        if (!isActive()) return;

        task.cancel();
        task = null;

        startCooldown();
        hotbarItem.show();

        end(player);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        reset();
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
    public void onMelee(AttributeDamageEvent event) {
        if (task == null) return;
        if (event.getAttribute().getPlayer() != player) return;
        if (!(event.getAttribute() instanceof Melee)) return;

        reset();

        event.getDamage().setDamage(config.getDouble("Damage"));
        event.getDamage().setKb(config.getInt("Kb"));
        event.getDamage().setKbY(config.getInt("KbY"));
        event.setAttribute(this);

        event.getVictim().getWorld().playSound(event.getVictim().getLocation(), Sound.EXPLODE, 3, 1);
        displayAttackEffect(event.getVictim());
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
