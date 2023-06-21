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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KitManager implements Listener {
    private static final String DEFAULT_KIT = "Barbarian";

    private final SSL plugin;

    private final Map<UUID, Kit> selectedKits = new HashMap<>();
    private final Map<String, Kit> kitsByName = new HashMap<>();
    private final Map<UUID, Kit> kitsByNpc = new HashMap<>();
    private final Map<String, NPC> npcsByKit = new HashMap<>();
    private final Map<UUID, Map<String, Hologram>> kitHolograms = new HashMap<>();

    public KitManager(SSL plugin) {
        this.plugin = plugin;
    }

    public void setupKits() {
        this.plugin.getResources().loadKits().forEach(this::setupKit);
    }

    private void setupKit(Kit kit) {
        this.kitsByName.put(kit.getConfigName(), kit);

        String locString = this.plugin.getResources().getLobby().getString("KitNpcs." + kit.getConfigName());
        Location location = YamlReader.location("lobby", locString);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, kit.getSkinName());
        npc.setName(kit.getBoldedDisplayName());

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(kit.getSkinName());
        npc.addTrait(skinTrait);

        npc.spawn(location);

        this.kitsByNpc.put(npc.getUniqueId(), kit);
        this.npcsByKit.put(kit.getConfigName(), npc);

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
        this.kitHolograms.put(player.getUniqueId(), new HashMap<>());

        this.kitsByName.forEach((name, kit) -> {
            Location location = this.npcsByKit.get(name).getStoredLocation();
            location.add(0, this.plugin.getResources().getConfig().getDouble("KitHologramHeight"), 0);

            Hologram hologram = HolographicDisplaysAPI.get(this.plugin).createHologram(location);
            hologram.getLines().appendText("");
            this.kitHolograms.get(player.getUniqueId()).put(name, hologram);
        });
    }

    public void updateHolograms(Player player) {

        this.kitHolograms.get(player.getUniqueId()).forEach((kitName, holo) -> updateAccessHologram(
                holo,
                this.getKitAccess(player, kitName),
                this.kitsByName.get(kitName)
        ));

        this.kitHolograms.forEach((otherUuid, holograms) -> {

            if (!otherUuid.equals(player.getUniqueId())) {

                for (Hologram holo : holograms.values()) {
                    holo.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
                }

                OfflinePlayer other = Bukkit.getOfflinePlayer(otherUuid);

                if (other.isOnline()) {

                    for (Hologram holo : this.kitHolograms.get(player.getUniqueId()).values()) {
                        holo.getVisibilitySettings()
                                .setIndividualVisibility((Player) other, VisibilitySettings.Visibility.HIDDEN);
                    }
                }
            }
        });
    }

    private static void updateAccessHologram(Hologram hologram, KitAccessType accessType, Kit kit) {
        hologram.getLines().remove(0);
        hologram.getLines().appendText(accessType.getHologram(kit));
    }

    public KitAccessType getKitAccess(Player player, String kit) {
        boolean isSame = this.getSelectedKit(player).getConfigName().equals(kit);
        return isSame ? KitAccessType.ALREADY_SELECTED : KitAccessType.ACCESS;
    }

    public Kit getSelectedKit(Player player) {
        return this.selectedKits.get(player.getUniqueId());
    }

    public void pullUserKit(Player player) {
        PlayerDatabase db = this.plugin.getPlayerDatabase();
        String kitString = db.getOrDefault(player.getUniqueId(), "kit", DEFAULT_KIT, DEFAULT_KIT);
        Kit kit = this.kitsByName.get(kitString);
        this.setKit(player, kit == null ? this.kitsByName.get("Barbarian") : kit);
    }

    public void setKit(Player player, Kit kit) {
        GameManager gameManager = this.plugin.getGameManager();
        GameState state = gameManager.getState();

        UUID uuid = player.getUniqueId();
        Kit newKit = this.plugin.getResources().loadKit(kit.getConfigName());

        Optional.ofNullable(this.selectedKits.put(uuid, newKit)).ifPresent(oldKit -> {
            oldKit.destroy();
            updateAccessHologram(this.kitHolograms.get(uuid).get(oldKit.getConfigName()), KitAccessType.ACCESS, oldKit);

            if (state.allowKitSelection() && state.isInArena()) {
                gameManager.getProfile(player).setKit(newKit);
            }

            if (state.updatesKitSkins()) {
                Skin oldSkin = oldKit.getSkin();
                Skin newSkin = newKit.getSkin();

                newSkin.updatePrevious(oldSkin.getPreviousTexture(), oldSkin.getPreviousSignature());
                Skin.apply(this.plugin, player, newSkin.getTexture(), newSkin.getSignature());
            }
        });

        newKit.equip(player);

        if (state instanceof InGameState) {
            newKit.activate();
        }

        Hologram accessHologram = this.kitHolograms.get(uuid).get(newKit.getConfigName());
        updateAccessHologram(accessHologram, KitAccessType.ALREADY_SELECTED, newKit);

        Chat.KIT.send(player, String.format("&7You have selected the %s &7kit.", newKit.getDisplayName()));
        newKit.getHurtNoise().playForPlayer(player);
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(this.kitHolograms.remove(player.getUniqueId()))
                .ifPresent(holograms -> holograms.values().forEach(Hologram::delete));

        Optional.ofNullable(this.selectedKits.remove(player.getUniqueId())).ifPresent(kit -> {
            kit.destroy();
            this.plugin.getPlayerDatabase().set(player.getUniqueId(), "kit", kit.getConfigName());
        });
    }

    public List<Kit> getKits() {
        return new ArrayList<>(this.kitsByName.values());
    }

    public Optional<Kit> getKitByName(String name) {
        return this.kitsByName.containsKey(name) ? Optional.of(this.kitsByName.get(name)) : Optional.empty();
    }

    public void destroyNpcs() {
        this.kitsByNpc.keySet().forEach(uuid -> CitizensAPI.getNPCRegistry().getByUniqueId(uuid).destroy());
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event) {
        UUID uuid = event.getNPC().getUniqueId();
        Optional.ofNullable(this.kitsByNpc.get(uuid)).ifPresent(npc -> this.setKit(event.getClicker(), npc));
    }
}
