package com.github.zilosz.ssl.util.effects;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeathNPC extends BukkitRunnable implements Listener {
  private final NPC npc;
  private final Player player;
  private final int duration;
  private final double velocity;
  private int ticks;

  public static DeathNPC spawn(SSL plugin, Player player) {
    Section death = plugin.getResources().getConfig().getSection("Death");

    NPC npc =
        SSL.getInstance().getNpcRegistry().createNPC(EntityType.PLAYER, player.getDisplayName());
    SSL.getInstance().getKitManager().getSelectedKit(player).getSkin().applyToNpc(npc);
    npc.spawn(player.getLocation());

    DeathNPC deathNPC =
        new DeathNPC(npc, player, death.getInt("Duration"), death.getDouble("Velocity"));
    deathNPC.runTaskTimer(plugin, 0, 0);
    Bukkit.getPluginManager().registerEvents(deathNPC, plugin);

    return deathNPC;
  }

  @Override
  public void run() {

    if (ticks++ >= duration) {
      destroy();
      return;
    }

    npc.getEntity().setVelocity(new Vector(0, velocity, 0));
    Location loc = npc.getStoredLocation().subtract(0, 0.4, 0);
    new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_LARGE)).show(loc);
    player.getWorld().playSound(npc.getStoredLocation(), Sound.FIREWORK_LAUNCH, 2, 2);
  }

  public void destroy() {
    if (!npc.isSpawned()) return;

    player.getWorld().playSound(npc.getStoredLocation(), Sound.WITHER_DEATH, 3, 1.5f);

    KitManager kitManager = SSL.getInstance().getKitManager();
    Color color = kitManager.getSelectedKit(player).getColor().getAwtColor();

    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color);
    Location center = EntityUtils.center(npc.getEntity());
    new ParticleMaker(particle).boom(SSL.getInstance(), center, 5, 0.25, 50);

    npc.destroy();

    HandlerList.unregisterAll(this);
    cancel();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDamage(DamageEvent event) {
    if (event.getVictim() == npc.getEntity()) {
      event.setCancelled(true);
    }
  }
}
