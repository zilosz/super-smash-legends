package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.config.Resources;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.game.state.GameStateType;
import com.github.zilosz.ssl.util.Skin;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.HologramLines;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KitManager implements Listener {
  private final List<Kit> kits = new ArrayList<>();
  private final Map<Player, Kit> selectedKits = new HashMap<>();
  private final Map<Player, Skin> originalSkins = new HashMap<>();
  private final Map<NPC, KitType> npcToKitType = new HashMap<>();
  private final Map<Player, EnumMap<KitType, Hologram>> kitHolograms = new HashMap<>();
  private final Map<Player, Skin.SelfSkinShower> skinShowers = new HashMap<>();
  private final Collection<Block> podiumBlocks = new ArrayList<>();

  public void destroyPodiums() {
    CollectionUtils.clearWhileIterating(podiumBlocks, block -> block.setType(Material.AIR));
  }

  public Skin getRealSkin(Player player) {
    return originalSkins.get(player);
  }

  public List<Kit> getKits() {
    return Collections.unmodifiableList(kits);
  }

  public void setupKits() {

    for (KitType kitType : KitType.values()) {
      Kit kit = createKit(kitType);
      kits.add(kit);

      NPCRegistry npcRegistry = SSL.getInstance().getNpcRegistry();
      NPC npc = npcRegistry.createNPC(EntityType.PLAYER, kit.getBoldedDisplayName());
      npcToKitType.put(npc, kitType);
      kit.getSkin().applyToNpc(npc);

      Resources resources = SSL.getInstance().getResources();
      String locString = resources.getLobby().getString("KitNpcs." + kitType.getConfigName());
      Location loc = YamlReader.location(CustomWorldType.LOBBY.getWorldName(), locString);

      npc.spawn(loc);

      Block beacon = loc.subtract(0, 1, 0).getBlock();
      beacon.setType(Material.BEACON);
      podiumBlocks.add(beacon);

      setPodiumSlab(loc, 1, 0);
      setPodiumSlab(loc, 0, 1);
      setPodiumSlab(loc, -1, 0);
      setPodiumSlab(loc, 0, -1);

      setPodiumWool(loc, 1, 0, kit);
      setPodiumWool(loc, -1, 0, kit);
      setPodiumWool(loc, 0, 1, kit);
      setPodiumWool(loc, 0, -1, kit);
    }

    kits.sort(Comparator.comparing(Kit::getType));
  }

  public Kit createKit(KitType kitType) {
    return new Kit(SSL.getInstance().getResources().getKitConfig(kitType), kitType);
  }

  private void setPodiumSlab(Location beacon, int x, int z) {
    Block block = beacon.clone().add(x, 0, z).getBlock();
    block.setType(Material.STEP);
    podiumBlocks.add(block);
  }

  @SuppressWarnings("deprecation")
  private void setPodiumWool(Location beacon, int x, int z, Kit kit) {
    Block block = beacon.clone().add(x, -1, z).getBlock();
    block.setType(Material.WOOL);
    block.setData(kit.getColor().getDyeColor().getWoolData());
    podiumBlocks.add(block);
  }

  public void createHolograms(Player player) {
    kitHolograms.put(player, new EnumMap<>(KitType.class));

    npcToKitType.forEach((npc, kitType) -> {
      Location location = npc.getStoredLocation();
      location.add(0, getConfig().getDouble("HologramHeight"), 0);
      Hologram hologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(location);
      kitHolograms.get(player).put(kitType, hologram);
    });
  }

  private Section getConfig() {
    return SSL.getInstance().getResources().getConfig().getSection("Kit");
  }

  public void updateHolograms(Player player) {
    kitHolograms.get(player).forEach((kitType, hologram) -> {
      updateAccessHologram(player, getKitAccess(player, kitType), kitType);
    });

    kitHolograms.forEach((other, holograms) -> {
      if (other == player) return;

      for (Hologram holo : holograms.values()) {
        holo
            .getVisibilitySettings()
            .setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
      }

      if (other.isOnline()) {
        for (Hologram holo : kitHolograms.get(player).values()) {
          holo
              .getVisibilitySettings()
              .setIndividualVisibility(other, VisibilitySettings.Visibility.HIDDEN);
        }
      }
    });
  }

  private void updateAccessHologram(Player player, KitAccess accessType, KitType kitType) {
    HologramLines lines = kitHolograms.get(player).get(kitType).getLines();
    lines.clear();
    lines.appendText(accessType.getHologram());
  }

  public KitAccess getKitAccess(Player player, KitType kitType) {
    return getSelectedKit(player).getType() == kitType ? KitAccess.SELECTED : KitAccess.ACCESSIBLE;
  }

  public Kit getSelectedKit(Player player) {
    return selectedKits.get(player);
  }

  public void loadAndSetUserKit(Player player) {
    KitType kitType;

    try {
      String kit = SSL.getInstance().getPlayerDatabase().getPlayerData(player).getKit();
      kitType = KitType.valueOf(kit);
    }
    catch (IllegalArgumentException | NullPointerException e) {
      kitType = KitType.valueOf(getConfig().getString("Default"));
    }

    setKit(player, kitType);
  }

  public Kit setKit(Player player, KitType kitType) {
    GameManager gameManager = SSL.getInstance().getGameManager();
    GameState state = gameManager.getState();

    Kit newKit = createKit(kitType);

    if (state.isPlaying()) {
      SSL.getInstance().getGameManager().getProfile(player).setKit(newKit);
    }

    Runnable noiseRunner = () -> newKit.getHurtNoise().playForPlayer(player);

    if (state.updatesKitSkins()) {
      Optional.ofNullable(skinShowers.remove(player)).ifPresent(Skin.SelfSkinShower::cancel);
      skinShowers.put(player, newKit.getSkin().apply(SSL.getInstance(), player, noiseRunner));
    }
    else {
      noiseRunner.run();
    }

    Optional.ofNullable(selectedKits.put(player, newKit)).ifPresentOrElse(kit -> {
      kit.destroy();
      updateAccessHologram(player, KitAccess.ACCESSIBLE, kit.getType());
    }, () -> originalSkins.put(player, Skin.fromPlayer(player)));

    newKit.equip(player);

    if (state.getType() == GameStateType.IN_GAME) {
      newKit.activate();
    }

    updateAccessHologram(player, KitAccess.SELECTED, kitType);

    Chat.KIT.send(player,
        String.format("&7You have selected the %s &7kit.", newKit.getDisplayName())
    );

    return newKit;
  }

  public void wipePlayer(Player player) {
    Optional
        .ofNullable(kitHolograms.remove(player))
        .ifPresent(holograms -> holograms.values().forEach(Hologram::delete));

    Optional.ofNullable(selectedKits.remove(player)).ifPresent(kit -> {
      kit.destroy();
      SSL.getInstance().getPlayerDatabase().getPlayerData(player).setKit(kit.getType().name());
    });
  }

  @EventHandler
  public void onNpcClick(NPCLeftClickEvent event) {
    Optional
        .ofNullable(npcToKitType.get(event.getNPC()))
        .ifPresent(kitType -> setKit(event.getClicker(), kitType));
  }
}
