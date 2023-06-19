package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.Effects;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
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
        return super.invalidate(event) || active;
    }

    public void reset() {
        if (!active) return;

        active = false;

        kit.getJump().setMaxCount(ogJumps);
        player.removePotionEffect(PotionEffectType.SPEED);

        firework.remove();
        particleTask.cancel();
        resetTask.cancel();

        player.playSound(player.getLocation(), Sound.WOLF_WHINE, 1, 1);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        active = true;

        ogJumps = kit.getJump().getMaxCount();
        kit.getJump().setMaxCount(ogJumps + config.getInt("ExtraJumps"));

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, config.getInt("Speed")));
        firework = Effects.launchFirework(player.getLocation(), Color.RED, 1);

        ParticleBuilder particle = new ParticleBuilder(EnumParticle.REDSTONE);
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> particle.ring(player.getLocation().add(0, 0.3, 0), 90, 0, 0.5, 20), 0, 5);

        player.playSound(player.getLocation(), Sound.WOLF_GROWL, 1, 1);

        resetTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reset();
            startCooldown();
        }, config.getInt("Duration"));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        reset();
    }

    @EventHandler
    public void onDamage(AttributeDamageEvent event) {
        if (this.active && event.getAttribute().getPlayer() == this.player) {
            double multiplier = config.getDouble("DamageMultiplier");
            event.getDamageSettings().setDamage(event.getDamageSettings().getDamage() * multiplier);
        }
    }
}
