package io.github.aura6.supersmashlegends.kit;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.InGameState;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
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
    private final Map<UUID, Kit> kitNpcs = new HashMap<>();

    public KitManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public void setupKits() {
        plugin.getResources().loadKits().forEach(this::setupKit);
    }

    @SuppressWarnings("deprecation")
    public void setupKit(Kit kit) {
        kitsByName.put(kit.getConfigName(), kit);

        String locString = plugin.getResources().getLobby().getString("KitNpcLocations." + kit.getConfigName());
        Location location = YamlReader.location("lobby", locString);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, kit.getSkinName());
        npc.setName(kit.getDisplayName());
        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(kit.getSkinName());
        npc.addTrait(skinTrait);
        npc.spawn(location);

        kitNpcs.put(npc.getUniqueId(), kit);
    }

    public void setupUser(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> kitNames = new ArrayList<>(kitsByName.keySet());
        List<String> owned = plugin.getDb().getOrDefault(uuid, "ownedKits", new ArrayList<>(), kitNames);
        ownedKits.put(uuid, new HashSet<>(owned));

        String chosenKit = plugin.getDb().getOrDefault(uuid, "chosenKit", "Barbarian", "Barbarian");
        setKit(player, kitsByName.get(chosenKit));
        giveOwnership(player, chosenKit);
    }

    public void uploadUser(Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getDb().setIfEnabled(uuid, "ownedKits", new ArrayList<>(ownedKits.get(uuid)));
        plugin.getDb().setIfEnabled(uuid, "chosenKit", selectedKits.get(uuid).getConfigName());
    }

    public List<Kit> getKits() {
        return new ArrayList<>(kitsByName.values());
    }

    public void setKit(Player player, Kit kit) {
        wipePlayerKit(player);

        Kit newKit = kit.copy();
        selectedKits.put(player.getUniqueId(), newKit);
        newKit.equip(player);

        if (plugin.getGameManager().getState() instanceof InGameState) {
            newKit.activate();
        }

        ownedKits.get(player.getUniqueId()).add(newKit.getConfigName());

        Chat.KIT.send(player, String.format("&7You have selected the %s &7kit.", newKit.getDisplayName()));
    }

    public Kit getSelectedKit(Player player) {
        return selectedKits.get(player.getUniqueId());
    }

    public boolean ownsKit(Player player, String name) {
        return ownedKits.get(player.getUniqueId()).contains(name);
    }

    public boolean ownsKit(Player player, Kit kit) {
        return ownsKit(player, kit.getConfigName());
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
        KitAccessType accessType = getKitAccess(player, kit);

        if (accessType == KitAccessType.ALREADY_SELECTED) {
            Chat.KIT.send(player, "&7You have already selected this kit.");

        } else if (accessType == KitAccessType.ACCESS || plugin.getEconomyManager().tryPurchase(player, kit.getPrice())) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            setKit(player, kit);
        }

        return accessType;
    }

    public KitAccessType getKitAccess(Player player, Kit kit) {
        if (getSelectedKit(player).getConfigName().equals(kit.getConfigName())) return KitAccessType.ALREADY_SELECTED;
        return ownsKit(player, kit) ? KitAccessType.ACCESS : KitAccessType.BUY;
    }

    public void destroyNpcs() {
        kitNpcs.keySet().forEach(uuid -> CitizensAPI.getNPCRegistry().getByUniqueId(uuid).destroy());
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event) {
        UUID uuid = event.getNPC().getUniqueId();

        if (kitNpcs.containsKey(uuid)) {
            handleKitSelection(event.getClicker(), kitNpcs.get(uuid));
        }
    }
}
