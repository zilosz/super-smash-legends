package com.github.zilosz.ssl.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
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
    private static final String PROFILE_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String UUID_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private final String texture;
    private final String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public static Skin fromPlayer(Player player) {
        GameProfile profile = NmsUtils.getPlayer(player).getProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        return new Skin(property.getValue(), property.getSignature());
    }

    public static Skin fromMojang(String playerName) {

        try {
            URL profileUrl = new URL(PROFILE_API_URL + playerName);
            InputStreamReader profileReader = new InputStreamReader(profileUrl.openStream());
            String uuid = new JsonParser().parse(profileReader).getAsJsonObject().get("id").getAsString();

            URL uuidUrl = new URL(UUID_API_URL + uuid + "?unsigned=false");
            JsonObject uuidJson = new JsonParser().parse(new InputStreamReader(uuidUrl.openStream())).getAsJsonObject();
            JsonObject textureProperty = uuidJson.get("properties").getAsJsonArray().get(0).getAsJsonObject();

            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new Skin(texture, signature);

        } catch (IOException e) {
            return Skin.fromMojang("Notch");
        }
    }

    public SelfSkinShower apply(Plugin plugin, Player player) {
        this.showToOthers(player);
        Location oldLoc = player.getLocation();
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 120, 0));
        return showToPlayer(plugin, player, () -> player.teleport(oldLoc));
    }

    private void showToOthers(Player player) {
        this.updateProfile(NmsUtils.getPlayer(player).getProfile());

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            other.showPlayer(player);
        }
    }

    private static SelfSkinShower showToPlayer(Plugin plugin, Player player, Runnable onTp) {
        EntityPlayer nmsPlayer = NmsUtils.getPlayer(player);

        NmsUtils.sendPacket(
                player,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nmsPlayer)
        );

        NmsUtils.sendPacket(
                player,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nmsPlayer)
        );

        Runnable skinRunnable = () -> NmsUtils.sendPacket(player, new PacketPlayOutRespawn(
                nmsPlayer.dimension,
                nmsPlayer.getWorld().getDifficulty(),
                nmsPlayer.getWorld().getWorldData().getType(),
                nmsPlayer.playerInteractManager.getGameMode()
        ));

        return new SelfSkinShower(skinRunnable, onTp).runLater(plugin);
    }

    private void updateProfile(GameProfile gameProfile) {
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", new Property("textures", this.texture, this.signature));
    }

    public void applyToSkull(SkullMeta meta) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        this.updateProfile(profile);

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public SelfSkinShower applyAcrossTeleport(Plugin plugin, Player player, Runnable onTp) {
        this.showToOthers(player);
        return showToPlayer(plugin, player, onTp);
    }

    public void applyToNpc(NPC npc) {
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("", this.signature, this.texture);
    }

    public static class SelfSkinShower {
        private BukkitTask showDelayer;
        private final Runnable skinRunnable;
        private final Runnable teleportRunnable;

        private SelfSkinShower(Runnable skinRunnable, Runnable teleportRunnable) {
            this.skinRunnable = skinRunnable;
            this.teleportRunnable = teleportRunnable;
        }

        private SelfSkinShower runLater(Plugin plugin) {
            this.showDelayer = Bukkit.getScheduler().runTaskLater(plugin, this::show, 2);
            return this;
        }

        public void show() {
            this.showWithoutTpAction();
            this.teleportRunnable.run();
        }

        public void showWithoutTpAction() {
            this.showDelayer.cancel();
            this.skinRunnable.run();
        }
    }
}
