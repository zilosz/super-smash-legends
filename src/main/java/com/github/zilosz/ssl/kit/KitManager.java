package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.game.state.InGameState;
import com.github.zilosz.ssl.utils.Skin;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.Chat;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KitManager implements Listener {
    private final Map<Player, Kit> selectedKits = new HashMap<>();
    private final Map<NPC, KitType> kitsPerNpc = new HashMap<>();
    private final Map<Player, Map<KitType, Hologram>> kitHolograms = new HashMap<>();

    public void setupKits() {
        for (KitType type : KitType.values()) {
            this.setupKit(type);
        }
    }

    private void setupKit(KitType kitType) {
        Kit kit = createKit(kitType);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, kit.getSkinName());
        this.kitsPerNpc.put(npc, kitType);
        npc.setName(kit.getBoldedDisplayName());

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(kit.getSkinName());
        npc.addTrait(skinTrait);

        String locString = SSL.getInstance().getResources().getLobby().getString("KitNpcs." + kit.getConfigName());
        Location location = YamlReader.location("lobby", locString);
        npc.spawn(location);

        location.subtract(0, 1, 0).getBlock().setType(Material.BEACON);

        this.setPodiumSlab(location, 1, 0);
        this.setPodiumSlab(location, 0, 1);
        this.setPodiumSlab(location, -1, 0);
        this.setPodiumSlab(location, 0, -1);

        this.setPodiumWool(location, 1, 0, kit);
        this.setPodiumWool(location, -1, 0, kit);
        this.setPodiumWool(location, 0, 1, kit);
        this.setPodiumWool(location, 0, -1, kit);
    }

    public static Kit createKit(KitType kitType) {
        return new Kit(SSL.getInstance().getResources().getKitConfig(kitType), kitType);
    }

    private void setPodiumSlab(Location beacon, int x, int z) {
        Block block = beacon.clone().add(x, 0, z).getBlock();
        block.setType(Material.STEP);
    }

    private void setPodiumWool(Location beacon, int x, int z, Kit kit) {
        Block block = beacon.clone().add(x, -1, z).getBlock();
        block.setType(Material.WOOL);
        block.setData(kit.getColor().getDyeColor().getWoolData());
    }

    public void createHolograms(Player player) {
        this.kitHolograms.put(player, new HashMap<>());

        this.kitsPerNpc.forEach((npc, kitType) -> {
            Location location = npc.getStoredLocation();
            location.add(0, SSL.getInstance().getResources().getConfig().getDouble("Kit.HologramHeight"), 0);

            Hologram hologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(location);
            hologram.getLines().appendText("");
            this.kitHolograms.get(player).put(kitType, hologram);
        });
    }

    public void updateHolograms(Player player) {

        this.kitHolograms.get(player).forEach((kitType, hologram) -> {
            this.updateAccessHologram(player, this.getKitAccess(player, kitType), kitType);
        });

        this.kitHolograms.forEach((other, holograms) -> {
            if (other == player) return;

            for (Hologram holo : holograms.values()) {
                holo.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
            }

            if (other.isOnline()) {

                for (Hologram holo : this.kitHolograms.get(player).values()) {
                    holo.getVisibilitySettings().setIndividualVisibility(other, VisibilitySettings.Visibility.HIDDEN);
                }
            }
        });
    }

    private void updateAccessHologram(Player player, KitAccessType accessType, KitType kitType) {
        Hologram hologram = this.kitHolograms.get(player).get(kitType);
        hologram.getLines().remove(0);
        hologram.getLines().appendText(accessType.getHologram());
    }

    public KitAccessType getKitAccess(Player player, KitType kitType) {
        Kit selectedKit = SSL.getInstance().getGameManager().getProfile(player).getKit();
        return selectedKit.getType() == kitType ? KitAccessType.SELECTED : KitAccessType.ACCESSIBLE;
    }

    public void pullUserKit(Player player) {
        PlayerDatabase db = SSL.getInstance().getPlayerDatabase();
        KitType defaultKit = KitType.valueOf(SSL.getInstance().getResources().getConfig().getString("Kit.Default"));
        String kitName = db.getOrDefault(player.getUniqueId(), "kit", defaultKit.name(), defaultKit.name());

        try {
            this.setKit(player, KitType.valueOf(kitName));

        } catch (IllegalArgumentException e) {
            this.setKit(player, defaultKit);
        }
    }

    public void setKit(Player player, KitType kitType) {
        GameManager gameManager = SSL.getInstance().getGameManager();
        GameState state = gameManager.getState();

        Kit newKit = createKit(kitType);

        Optional.ofNullable(this.selectedKits.put(player, newKit)).ifPresent(oldKit -> {
            oldKit.destroy();
            this.updateAccessHologram(player, KitAccessType.ACCESSIBLE, kitType);

            if (state.allowKitSelection() && state.isInArena()) {
                gameManager.getProfile(player).setKit(newKit);
            }

            if (state.updatesKitSkins()) {
                Skin oldSkin = oldKit.getSkin();
                Skin newSkin = newKit.getSkin();

                newSkin.updatePrevious(oldSkin.getPreviousTexture(), oldSkin.getPreviousSignature());
                Skin.apply(SSL.getInstance(), player, newSkin.getTexture(), newSkin.getSignature());
            }
        });

        newKit.equip(player);

        if (state instanceof InGameState) {
            newKit.activate();
        }

        this.updateAccessHologram(player, KitAccessType.SELECTED, kitType);

        Chat.KIT.send(player, String.format("&7You have selected the %s &7kit.", newKit.getDisplayName()));
        newKit.getHurtNoise().playForPlayer(player);
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(this.kitHolograms.remove(player))
                .ifPresent(holograms -> holograms.values().forEach(Hologram::delete));

        Optional.ofNullable(this.selectedKits.remove(player)).ifPresent(kit -> {
            kit.destroy();
            SSL.getInstance().getPlayerDatabase().set(player.getUniqueId(), "kit", kit.getConfigName());
        });
    }

    public void destroyNpcs() {
        this.kitsPerNpc.keySet().forEach(NPC::destroy);
    }

    public Kit getSelectedKit(Player player) {
        return this.selectedKits.get(player);
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event) {
        Optional.ofNullable(this.kitsPerNpc.get(event.getNPC()))
                .ifPresent(kitType -> this.setKit(event.getClicker(), kitType));
    }
}
