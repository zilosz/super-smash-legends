package com.github.zilosz.ssl.utils;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListener;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NmsUtils {

    public static net.minecraft.server.v1_8_R3.Entity getEntity(Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public static EntityLiving getLiving(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle();
    }

    public static net.minecraft.server.v1_8_R3.World getWorld(World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static void broadcastPacket(Packet<? extends PacketListener> packet) {
        Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, packet));
    }

    public static void sendPacket(Player player, Packet<? extends PacketListener> packet) {
        getConnection(player).sendPacket(packet);
    }

    public static PlayerConnection getConnection(Player player) {
        return getPlayer(player).playerConnection;
    }

    public static EntityPlayer getPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
