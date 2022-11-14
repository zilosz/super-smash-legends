package io.github.aura6.supersmashlegends.utils;

import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class Hologram extends BukkitRunnable implements Listener {
    private final Plugin plugin;
    private EntityArmorStand armorStand;

    public Hologram(Plugin plugin) {
        this.plugin = plugin;
    }

    public abstract Location updateLocation();

    public void spawn() {
        Location location = updateLocation();
        World nmsWorld = NmsUtils.getWorld(location.getWorld());

        armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setCustomNameVisible(true);

        NBTTagCompound tag = new NBTTagCompound();
        armorStand.c(tag);
        tag.setBoolean("Marker", true);
        armorStand.f(tag);

        nmsWorld.addEntity(armorStand);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        runTaskTimer(plugin, 0, 0);
    }

    public void destroy() {
        if (armorStand != null) {
            armorStand.die();
            HandlerList.unregisterAll(this);
            cancel();
        }
    }

    public void hideFrom(Player player) {
        if (armorStand != null) {
            NmsUtils.sendPacket(player, new PacketPlayOutEntityDestroy(1, armorStand.getId()));
        }
    }

    public void setText(String text) {
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
    }

    @Override
    public void run() {
        armorStand.getBukkitEntity().setVelocity(new Vector(0, 0, 0));
        Location location = updateLocation();
        armorStand.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() == armorStand.getBukkitEntity()) {
            event.setCancelled(true);
        }
    }
}
