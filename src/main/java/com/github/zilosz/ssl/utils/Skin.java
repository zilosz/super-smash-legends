package com.github.zilosz.ssl.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.UUID;

@Getter
public class Skin {
    private final String texture;
    private final String signature;
    private String previousTexture;
    private String previousSignature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public void updatePrevious(String texture, String signature) {
        this.previousTexture = texture;
        this.previousSignature = signature;
    }

    public static void updateProfile(GameProfile gameProfile, String texture, String signature) {
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
    }

    public void applyToSkull(SkullMeta meta) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        updateProfile(profile, this.texture, this.signature);

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void showToOthers(Player player, String texture, String signature) {
        updateProfile(NmsUtils.getPlayer(player).getProfile(), texture, signature);

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            other.showPlayer(player);
        }
    }

    private static void sendAddRemovePackets(Player player) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);

        NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nmsPlayer));
        NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nmsPlayer));

    }

    private static BukkitTask fakeRespawnAfterDelay(Plugin plugin, Player player, Runnable onTp) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);

        return Bukkit.getScheduler().runTaskLater(plugin, () -> {
            onTp.run();

            NmsUtils.sendPacket(player, new PacketPlayOutRespawn(
                    nmsPlayer.dimension,
                    nmsPlayer.getWorld().getDifficulty(),
                    nmsPlayer.getWorld().getWorldData().getType(),
                    nmsPlayer.playerInteractManager.getGameMode()));
        }, 2);
    }

    private static BukkitTask showToSelf(Plugin plugin, Player player, Runnable onTp) {
        sendAddRemovePackets(player);
        return fakeRespawnAfterDelay(plugin, player, onTp);
    }

    public static BukkitTask applyAcrossTp(Plugin plugin, Player player, String texture, String signature, Runnable onTp) {
        showToOthers(player, texture, signature);
        return showToSelf(plugin, player, onTp);
    }

    public static void apply(Plugin plugin, Player player, String texture, String signature) {
        showToOthers(player, texture, signature);
        Location oldLoc = player.getLocation();
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 120, 0));
        showToSelf(plugin, player, () -> player.teleport(oldLoc));
    }

    public BukkitTask applyAcrossTp(Plugin plugin, Player player, Runnable onTp) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);
        GameProfile profile = nmsPlayer.getProfile();

        Property property = profile.getProperties().get("textures").iterator().next();
        this.previousSignature = property.getSignature();
        this.previousTexture = property.getValue();

        return applyAcrossTp(plugin, player, this.texture, this.signature, onTp);
    }

    public static Skin fromMojang(String playerName) {

        try {
            URL profileUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            InputStreamReader profileReader = new InputStreamReader(profileUrl.openStream());
            String uuid = new JsonParser().parse(profileReader).getAsJsonObject().get("id").getAsString();

            URL uuidUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JsonObject uuidJson = new JsonParser().parse(new InputStreamReader(uuidUrl.openStream())).getAsJsonObject();
            JsonObject textureProperty = uuidJson.get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new Skin(texture, signature);

        } catch (IOException e) {
            return fromMojang("Notch");
        }
    }
}
