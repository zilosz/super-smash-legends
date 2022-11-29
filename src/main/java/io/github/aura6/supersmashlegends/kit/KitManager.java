package io.github.aura6.supersmashlegends.kit;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.InGameState;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class KitManager implements Listener {
    private final SuperSmashLegends plugin;

    private final Map<UUID, Kit> selectedKits = new HashMap<>();
    private final Map<UUID, Set<String>> ownedKits = new HashMap<>();

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

    public void setupUser(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> kitNames = new ArrayList<>(kitsByName.keySet());
        List<String> owned = plugin.getDb().getOrDefault(uuid, "ownedKits", new ArrayList<>(), kitNames);
        ownedKits.put(uuid, new HashSet<>(owned));

        String chosenKit = plugin.getDb().getOrDefault(uuid, "chosenKit", "Barbarian", "Barbarian");
        giveOwnership(player, chosenKit);

        kitHolograms.put(uuid, new HashMap<>());

        kitsByName.forEach((name, kit) -> {
            Location location = npcsByKit.get(name).getStoredLocation();
            location.add(0, plugin.getResources().getConfig().getDouble("KitHologramHeight"), 0);

            Hologram hologram = HolographicDisplaysAPI.get(plugin).createHologram(location);
            hologram.getLines().appendText("");
            kitHolograms.get(uuid).put(name, hologram);

            if (kit.getPrice() == 0) {
                giveOwnership(player, name);
            }
        });

        Kit chosen = kitsByName.get(chosenKit);
        setKit(player, chosen);

        kitHolograms.get(uuid).forEach((kitName, holo) ->
                updateAccessHologram(holo, getKitAccess(player, kitName), kitsByName.get(kitName)));

        kitHolograms.forEach((otherUuid, holograms) -> {

            if (!otherUuid.equals(uuid)) {

                for (Hologram holo : holograms.values()) {
                    holo.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
                }

                OfflinePlayer other = Bukkit.getOfflinePlayer(otherUuid);

                if (other.isOnline()) {

                    for (Hologram holo : kitHolograms.get(uuid).values()) {
                        holo.getVisibilitySettings().setIndividualVisibility((Player) other, VisibilitySettings.Visibility.HIDDEN);
                    }
                }
            }
        });
    }

    public void endUser(Player player) {
        UUID uuid = player.getUniqueId();
        kitHolograms.remove(uuid).values().forEach(Hologram::delete);
        plugin.getDb().setIfEnabled(uuid, "ownedKits", new ArrayList<>(ownedKits.get(uuid)));
        plugin.getDb().setIfEnabled(uuid, "chosenKit", selectedKits.get(uuid).getConfigName());
    }

    public List<Kit> getKits() {
        return new ArrayList<>(kitsByName.values());
    }

    public void setKit(Player player, Kit kit) {
        UUID uuid = player.getUniqueId();

        if (selectedKits.containsKey(uuid)) {
            Kit oldKit = selectedKits.get(player.getUniqueId());
            updateAccessHologram(kitHolograms.get(uuid).get(oldKit.getConfigName()), KitAccessType.ACCESS, oldKit);
        }

        wipePlayerKit(player);

        Kit newKit = kit.copy();
        selectedKits.put(uuid, newKit);
        newKit.equip(player);

        if (plugin.getGameManager().getState() instanceof InGameState) {
            newKit.activate();
        }

        ownedKits.get(uuid).add(newKit.getConfigName());
        updateAccessHologram(kitHolograms.get(uuid).get(kit.getConfigName()), KitAccessType.ALREADY_SELECTED, newKit);

        Chat.KIT.send(player, String.format("&7You have selected the %s &7kit.", newKit.getDisplayName()));
    }

    public Kit getSelectedKit(Player player) {
        return selectedKits.get(player.getUniqueId());
    }

    public boolean ownsKit(Player player, String name) {
        return ownedKits.get(player.getUniqueId()).contains(name);
    }

    public void giveOwnership(Player player, String name) {
        ownedKits.get(player.getUniqueId()).add(name);
    }

    public void wipePlayerKit(Player player) {
        Optional.ofNullable(selectedKits.remove(player.getUniqueId())).ifPresent(kit -> {
            kit.unequip();
            if (plugin.getGameManager().getState() instanceof InGameState) {
                kit.deactivate();
            }
        });
    }

    public Optional<Kit> getKitByName(String name) {
        return kitsByName.containsKey(name) ? Optional.of(kitsByName.get(name)) : Optional.empty();
    }

    public KitAccessType handleKitSelection(Player player, Kit kit) {
        KitAccessType accessType = getKitAccess(player, kit.getConfigName());

        if (accessType == KitAccessType.ALREADY_SELECTED) {
            Chat.KIT.send(player, "&7You have already selected this kit.");

        } else if (accessType == KitAccessType.ACCESS || plugin.getEconomyManager().tryPurchase(player, kit.getPrice())) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            setKit(player, kit);
        }

        return accessType;
    }

    public KitAccessType getKitAccess(Player player, String kit) {
        if (getSelectedKit(player).getConfigName().equals(kit)) return KitAccessType.ALREADY_SELECTED;
        return ownsKit(player, kit) ? KitAccessType.ACCESS : KitAccessType.BUY;
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
