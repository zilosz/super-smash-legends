package io.github.aura6.supersmashlegends.kit;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.game.state.GameState;
import io.github.aura6.supersmashlegends.game.state.InGameState;
import io.github.aura6.supersmashlegends.game.state.LobbyState;
import io.github.aura6.supersmashlegends.utils.Skin;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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
    private final SuperSmashLegends plugin;

    private final Map<UUID, Kit> selectedKits = new HashMap<>();
    private final Map<String, Kit> kitsByName = new HashMap<>();
    private final Map<UUID, Kit> kitsByNpc = new HashMap<>();
    private final Map<String, NPC> npcsByKit = new HashMap<>();
    private final Map<UUID, Map<String, Hologram>> kitHolograms = new HashMap<>();

    public KitManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    private void setupKit(Kit kit) {
        kitsByName.put(kit.getConfigName(), kit);

        String locString = plugin.getResources().getLobby().getString("KitNpcs." + kit.getConfigName());
        Location location = YamlReader.location("lobby", locString);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, kit.getSkinName());
        npc.setName(kit.getBoldedDisplayName());
        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(kit.getSkinName());
        npc.addTrait(skinTrait);
        npc.spawn(location);

        kitsByNpc.put(npc.getUniqueId(), kit);
        npcsByKit.put(kit.getConfigName(), npc);
    }

    public void setupKits() {
        plugin.getResources().loadKits().forEach(this::setupKit);
    }

    private static void updateAccessHologram(Hologram hologram, KitAccessType accessType, Kit kit) {
        hologram.getLines().remove(0);
        hologram.getLines().appendText(accessType.getHologram(kit));
    }

    public void createHolograms(Player player) {
        kitHolograms.put(player.getUniqueId(), new HashMap<>());

        kitsByName.forEach((name, kit) -> {
            Location location = npcsByKit.get(name).getStoredLocation();
            location.add(0, plugin.getResources().getConfig().getDouble("KitHologramHeight"), 0);

            Hologram hologram = HolographicDisplaysAPI.get(plugin).createHologram(location);
            hologram.getLines().appendText("");
            kitHolograms.get(player.getUniqueId()).put(name, hologram);
        });
    }

    public void updateHolograms(Player player) {
        kitHolograms.get(player.getUniqueId()).forEach((kitName, holo) ->
                updateAccessHologram(holo, getKitAccess(player, kitName), kitsByName.get(kitName)));

        kitHolograms.forEach((otherUuid, holograms) -> {

            if (!otherUuid.equals(player.getUniqueId())) {

                for (Hologram holo : holograms.values()) {
                    holo.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
                }

                OfflinePlayer other = Bukkit.getOfflinePlayer(otherUuid);

                if (other.isOnline()) {

                    for (Hologram holo : kitHolograms.get(player.getUniqueId()).values()) {
                        holo.getVisibilitySettings().setIndividualVisibility((Player) other, VisibilitySettings.Visibility.HIDDEN);
                    }
                }
            }
        });
    }

    public void pullUserKit(Player player) {
        String kitString = plugin.getDb().getOrDefault(player.getUniqueId(), "kit", "Barbarian", "Barbarian");
        Kit kit = this.kitsByName.get(kitString);
        setKit(player, kit == null ? this.kitsByName.get("Barbarian") : kit);
    }

    public void wipePlayer(Player player) {
        Optional.ofNullable(this.kitHolograms.remove(player.getUniqueId()))
                .ifPresent(holograms -> holograms.values().forEach(Hologram::delete));

        Optional.ofNullable(this.selectedKits.remove(player.getUniqueId())).ifPresent(kit -> {
            kit.destroy();
            this.plugin.getDb().setIfEnabled(player.getUniqueId(), "kit", kit.getConfigName());
        });
    }

    public List<Kit> getKits() {
        return new ArrayList<>(kitsByName.values());
    }

    public void setKit(Player player, Kit kit) {
        GameManager gameManager = this.plugin.getGameManager();
        GameState state = gameManager.getState();

        if (!(state instanceof InGameState) && !(state instanceof LobbyState)) {
            Chat.KIT.send(player, "&7You cannot set your kit at this time.");
            return;
        }

        UUID uuid = player.getUniqueId();
        Kit newKit = kit.copy();

        Optional.ofNullable(this.selectedKits.put(uuid, newKit)).ifPresent(oldKit -> {
            oldKit.destroy();
            updateAccessHologram(this.kitHolograms.get(uuid).get(oldKit.getConfigName()), KitAccessType.ACCESS, oldKit);

            if (state instanceof InGameState) {
                Skin oldSkin = oldKit.getSkin();
                Skin newSkin = newKit.getSkin();

                newSkin.updatePrevious(oldSkin.getPreviousTexture(), oldSkin.getPreviousSignature());
                Skin.apply(this.plugin, player, newSkin.getTexture(), newSkin.getSignature());

                gameManager.getProfile(player).setKit(newKit);
            }
        });

        newKit.equip(player);

        if (state instanceof InGameState) {
            newKit.activate();
        }

        Hologram accessHologram = this.kitHolograms.get(uuid).get(newKit.getConfigName());
        updateAccessHologram(accessHologram, KitAccessType.ALREADY_SELECTED, newKit);

        Chat.KIT.send(player, String.format("&7You have selected the %s &7kit.", newKit.getDisplayName()));
    }

    public Kit getSelectedKit(Player player) {
        return selectedKits.get(player.getUniqueId());
    }

    public Optional<Kit> getKitByName(String name) {
        return kitsByName.containsKey(name) ? Optional.of(kitsByName.get(name)) : Optional.empty();
    }

    public KitAccessType handleKitSelection(Player player, Kit kit) {
        KitAccessType accessType = getKitAccess(player, kit.getConfigName());

        if (accessType == KitAccessType.ALREADY_SELECTED) {
            Chat.KIT.send(player, "&7You have already selected this kit.");

        } else if (accessType == KitAccessType.ACCESS) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            setKit(player, kit);
        }

        return accessType;
    }

    public KitAccessType getKitAccess(Player player, String kit) {
        return getSelectedKit(player).getConfigName().equals(kit) ? KitAccessType.ALREADY_SELECTED : KitAccessType.ACCESS;
    }

    public void destroyNpcs() {
        kitsByNpc.keySet().forEach(uuid -> CitizensAPI.getNPCRegistry().getByUniqueId(uuid).destroy());
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event) {
        UUID uuid = event.getNPC().getUniqueId();

        if (kitsByNpc.containsKey(uuid)) {
            handleKitSelection(event.getClicker(), kitsByNpc.get(uuid));
        }
    }
}
