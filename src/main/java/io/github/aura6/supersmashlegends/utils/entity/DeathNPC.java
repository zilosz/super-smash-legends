package io.github.aura6.supersmashlegends.utils.entity;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.minecraft.server.v1_8_R3.EnumParticle;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DeathNPC extends BukkitRunnable implements Listener {
    private final Plugin plugin;
    private final NPC npc;
    private final Player player;
    private final int duration;
    private final double velocity;
    private int ticks = 0;

    private DeathNPC(Plugin plugin, NPC npc, Player player, int duration, double velocity) {
        this.plugin = plugin;
        this.npc = npc;
        this.player = player;
        this.duration = duration;
        this.velocity = velocity;
    }

    public void destroy() {
        if (!npc.isSpawned()) return;

        Location loc = npc.getStoredLocation();
        player.getWorld().playSound(loc, Sound.WITHER_DEATH, 3, 1.5f);
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 0, 255).boom(plugin, loc, 5, 0.25, 30);

        npc.destroy();

        HandlerList.unregisterAll(this);
        cancel();
    }

    @Override
    public void run() {

        if (ticks++ >= duration) {
            destroy();
            return;
        }

        npc.getEntity().setVelocity(new Vector(0, velocity, 0));
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(npc.getStoredLocation().subtract(0, 0.4, 0));
        player.getWorld().playSound(npc.getStoredLocation(), Sound.FIREWORK_LAUNCH, 2, 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNormalDamage(EntityDamageEvent event) {
        if (event.getEntity() == npc.getEntity()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCustomDamage(DamageEvent event) {
        if (event.getVictim() == npc.getEntity()) {
            event.setCancelled(true);
        }
    }

    public static DeathNPC spawn(SuperSmashLegends plugin, Player player) {
        Section death = plugin.getResources().getConfig().getSection("Death");
        String skin = death.getString("Skin");

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, skin);
        npc.setName(player.getName());

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(skin);
        npc.addTrait(skinTrait);
        npc.spawn(player.getLocation());

        DeathNPC deathNPC = new DeathNPC(plugin, npc, player, death.getInt("Duration"), death.getDouble("Velocity"));
        deathNPC.runTaskTimer(plugin, 0, 0);
        Bukkit.getPluginManager().registerEvents(deathNPC, plugin);

        return deathNPC;
    }
}
