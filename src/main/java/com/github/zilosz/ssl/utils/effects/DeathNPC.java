package com.github.zilosz.ssl.utils.effects;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class DeathNPC extends BukkitRunnable implements Listener {
    private final NPC npc;
    private final Player player;
    private final int duration;
    private final double velocity;
    private int ticks = 0;

    private DeathNPC(NPC npc, Player player, int duration, double velocity) {
        this.npc = npc;
        this.player = player;
        this.duration = duration;
        this.velocity = velocity;
    }

    public static DeathNPC spawn(SSL plugin, Player player) {
        Section death = plugin.getResources().getConfig().getSection("Death");

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Death NPC");
        SSL.getInstance().getNpcStorage().addNpc(npc);

        npc.setName(player.getName());

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(plugin.getKitManager().getSelectedKit(player).getSkinName());
        npc.addTrait(skinTrait);
        npc.spawn(player.getLocation());

        DeathNPC deathNPC = new DeathNPC(npc, player, death.getInt("Duration"), death.getDouble("Velocity"));
        deathNPC.runTaskTimer(plugin, 0, 0);
        Bukkit.getPluginManager().registerEvents(deathNPC, plugin);

        return deathNPC;
    }

    @Override
    public void run() {

        if (this.ticks++ >= this.duration) {
            this.destroy();
            return;
        }

        this.npc.getEntity().setVelocity(new Vector(0, this.velocity, 0));
        Location loc = this.npc.getStoredLocation().subtract(0, 0.4, 0);
        new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_LARGE)).show(loc);
        this.player.getWorld().playSound(this.npc.getStoredLocation(), Sound.FIREWORK_LAUNCH, 2, 2);
    }

    public void destroy() {
        if (!this.npc.isSpawned()) return;

        this.player.getWorld().playSound(this.npc.getStoredLocation(), Sound.WITHER_DEATH, 3, 1.5f);

        KitManager kitManager = SSL.getInstance().getKitManager();
        Color color = kitManager.getSelectedKit(this.player).getColor().getParticleColor();

        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color);
        new ParticleMaker(particle).boom(SSL.getInstance(), EntityUtils.center(this.npc.getEntity()), 5, 0.25, 50);

        this.npc.destroy();
        SSL.getInstance().getNpcStorage().removeNpc(this.npc);

        HandlerList.unregisterAll(this);
        this.cancel();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNormalDamage(EntityDamageEvent event) {
        if (event.getEntity() == this.npc.getEntity()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCustomDamage(DamageEvent event) {
        if (event.getVictim() == this.npc.getEntity()) {
            event.setCancelled(true);
        }
    }
}
