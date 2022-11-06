package io.github.aura6.supersmashlegends.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class Skin {
    private final String texture;
    private final String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public void updateProfile(GameProfile profile) {
        profile.getProperties().put("textures", new Property("textures", texture, signature));
    }

    public void apply(Plugin plugin, Player player) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayerHandle(player);
        GameProfile profile = nmsPlayer.getProfile();
        profile.getProperties().removeAll("textures");
        updateProfile(profile);

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            other.showPlayer(player);
        }

        Location original = player.getLocation();
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 255, 0));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(original);
            NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nmsPlayer));
            NmsUtils.sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nmsPlayer));
        }, 3);
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
