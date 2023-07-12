package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.game.state.GameStateType;
import com.github.zilosz.ssl.utils.Skin;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.world.StaticWorldType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.HologramLines;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KitManager implements Listener {
    private final List<Kit> kits = new ArrayList<>();
    private final Map<Player, Kit> selectedKits = new HashMap<>();
    private final Map<Player, Skin> realSkins = new HashMap<>();
    private final Map<NPC, KitType> kitsPerNpc = new HashMap<>();
    private final Map<Player, Map<KitType, Hologram>> kitHolograms = new HashMap<>();

    public Skin getRealSkin(Player player) {
        return this.realSkins.get(player);
    }

    public List<Kit> getKits() {
        return Collections.unmodifiableList(this.kits);
    }

    public void setupKits() {
        for (KitType type : KitType.values()) {
            this.setupKit(type);
        }
        this.kits.sort(Comparator.comparing(kit -> kit.getType().name()));
    }

    private void setupKit(KitType kitType) {
        Kit kit = this.createKit(kitType);
        this.kits.add(kit);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, kit.getBoldedDisplayName());
        SSL.getInstance().getNpcStorage().addNpc(npc);
        this.kitsPerNpc.put(npc, kitType);

        kit.getSkin().applyToNpc(npc);

        Section lookConfig = SSL.getInstance().getResources().getConfig().getSection("Kit.LookClose");
        LookClose lookClose = npc.getOrAddTrait(LookClose.class);
        lookClose.setRandomLook(lookConfig.getBoolean("Enabled"));
        lookClose.setRange(lookConfig.getDouble("Range"));
        lookClose.setRandomLookDelay(lookConfig.getInt("RandomLookDelay"));

        String locString = SSL.getInstance().getResources().getLobby().getString("KitNpcs." + kitType.getConfigName());
        Location location = YamlReader.location(StaticWorldType.LOBBY.getWorldName(), locString);
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

    public Kit createKit(KitType kitType) {
        return new Kit(SSL.getInstance().getResources().getKitConfig(kitType), kitType);
    }

    private void setPodiumSlab(Location beacon, int x, int z) {
        beacon.clone().add(x, 0, z).getBlock().setType(Material.STEP);
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
        HologramLines lines = this.kitHolograms.get(player).get(kitType).getLines();
        lines.clear();
        lines.appendText(accessType.getHologram());
    }

    public KitAccessType getKitAccess(Player player, KitType kitType) {
        return this.getSelectedKit(player).getType() == kitType ? KitAccessType.SELECTED : KitAccessType.ACCESSIBLE;
    }

    public Kit getSelectedKit(Player player) {
        return this.selectedKits.get(player);
    }

    public void pullUserKit(Player player) {
        PlayerDatabase db = SSL.getInstance().getPlayerDatabase();
        String defaultName = SSL.getInstance().getResources().getConfig().getString("Kit.Default");
        String kitName = db.getOrDefault(player.getUniqueId(), "kit", defaultName, defaultName);

        KitType kitType;

        try {
            kitType = KitType.valueOf(kitName);

        } catch (IllegalArgumentException e) {
            kitType = KitType.valueOf(defaultName);
        }

        this.setKit(player, kitType);
    }

    public void setKit(Player player, KitType kitType) {
        GameManager gameManager = SSL.getInstance().getGameManager();
        GameState state = gameManager.getState();

        Kit newKit = this.createKit(kitType);

        if (state.isPlaying()) {
            SSL.getInstance().getGameManager().getProfile(player).setKit(newKit);
        }

        if (state.updatesKitSkins()) {
            newKit.getSkin().apply(SSL.getInstance(), player);
        }

        Optional.ofNullable(this.selectedKits.put(player, newKit)).ifPresentOrElse(oldKit -> {
            oldKit.destroy();
            this.updateAccessHologram(player, KitAccessType.ACCESSIBLE, oldKit.getType());
        }, () -> {
            this.realSkins.put(player, Skin.fromPlayer(player));
        });

        newKit.equip(player);

        if (state.getType() == GameStateType.IN_GAME) {
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
            SSL.getInstance().getPlayerDatabase().set(player.getUniqueId(), "kit", kit.getType().name());
        });
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event) {
        Optional<KitType> kitType = Optional.ofNullable(this.kitsPerNpc.get(event.getNPC()));
        kitType.ifPresent(type -> this.setKit(event.getClicker(), type));
    }
}
