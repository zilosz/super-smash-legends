package io.github.aura6.supersmashlegends.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class Skin {
    private final String texture;
    private final String signature;
    private String previousTexture;
    private String previousSignature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public static BukkitTask apply(Plugin plugin, Player player, String texture, String signature, Runnable onTp) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);
        GameProfile profile = nmsPlayer.getProfile();

        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", texture, signature));

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            other.showPlayer(player);
        }

        return Bukkit.getScheduler().runTaskLater(plugin, () -> {
            onTp.run();

            NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nmsPlayer));
            NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nmsPlayer));
        }, 10);
    }

    public BukkitTask apply(Plugin plugin, Player player, Runnable tpFunc) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);
        GameProfile profile = nmsPlayer.getProfile();

        Property property = profile.getProperties().get("textures").iterator().next();
        this.previousSignature = property.getSignature();
        this.previousTexture = property.getValue();

        return apply(plugin, player, this.texture, this.signature, tpFunc);
    }

    public BukkitTask restorePrevious(Plugin plugin, Player player, Runnable tpFunc) {
        return apply(plugin, player, this.previousTexture, this.previousSignature, tpFunc);
    }

    public static Optional<Skin> fromMojang(String playerName) {

        try {
            URL profileUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            InputStreamReader profileReader = new InputStreamReader(profileUrl.openStream());
            String uuid = new JsonParser().parse(profileReader).getAsJsonObject().get("id").getAsString();

            URL uuidUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JsonObject uuidJson = new JsonParser().parse(new InputStreamReader(uuidUrl.openStream())).getAsJsonObject();
            JsonObject textureProperty = uuidJson.get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return Optional.of(new Skin(texture, signature));

        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
