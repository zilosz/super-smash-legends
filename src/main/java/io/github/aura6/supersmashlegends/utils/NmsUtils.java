package io.github.aura6.supersmashlegends.utils;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListener;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NmsUtils {

    public static EntityLiving getLivingHandle(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle();
    }

    public static EntityPlayer getPlayerHandle(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static PlayerConnection getConnection(Player player) {
        return getPlayerHandle(player).playerConnection;
    }

    public static void sendPacket(Player player, Packet<? extends PacketListener> packet) {
        getConnection(player).sendPacket(packet);
    }
}
