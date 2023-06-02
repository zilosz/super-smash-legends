package io.github.aura6.supersmashlegends.utils.entity;

import com.mojang.authlib.GameProfile;
import io.github.aura6.supersmashlegends.utils.NmsUtils;
import io.github.aura6.supersmashlegends.utils.Skin;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.stream.Stream;

public class FakeNpc {
    private EntityPlayer npc;
    private Location location;

    public void spawn(Location location) {
        if (this.npc != null) return;

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        MinecraftServer mcServer = MinecraftServer.getServer();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer.b());

        this.npc = new EntityPlayer(mcServer, worldServer, gameProfile, interactManager);
        this.npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        this.location = location;
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public void showTo(Player player) {
        if (this.npc == null) return;

        PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.npc);
        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(this.npc);
        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this.npc);

        NmsUtils.sendPacket(player, infoPacket);
        NmsUtils.sendPacket(player, spawnPacket);
        NmsUtils.sendPacket(player, removePacket);
    }

    private Stream<? extends Player> getPlayersInWorld() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getWorld() == this.npc.getWorld().getWorld());
    }

    public void showToAll() {
        this.getPlayersInWorld().forEach(this::showTo);
    }

    public void destroyFor(Player player) {
        if (this.npc != null) {
            NmsUtils.sendPacket(player, new PacketPlayOutEntityDestroy(this.npc.getId()));
        }
    }

    public void destroyForAll() {
        this.getPlayersInWorld().forEach(this::destroyFor);
    }

    public void moveRelative(Player player, Vector vector) {
        if (this.npc == null) return;

        byte x = (byte) (vector.getX() * 32.0D);
        byte y = (byte) (vector.getY() * 32.0D);
        byte z = (byte) (vector.getZ() * 32.0D);

        NmsUtils.sendPacket(player, new PacketPlayOutEntity.PacketPlayOutRelEntityMove(this.npc.getId(), x, y, z, false));
        this.location.add(vector.getX(), vector.getY(), vector.getZ());
    }

    public void moveRelativeForAll(Vector vector) {
        this.getPlayersInWorld().forEach(player -> this.moveRelative(player, vector));
    }

    public void setSkin(Skin skin) {
        if (this.npc != null) {
            Skin.updateGameProfile(this.npc, skin.getTexture(), skin.getSignature());
        }
    }
}
