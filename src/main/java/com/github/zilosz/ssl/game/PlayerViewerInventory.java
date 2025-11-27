package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.Skin;
import com.github.zilosz.ssl.util.inventory.AutoUpdatesHard;
import com.github.zilosz.ssl.util.inventory.AutoUpdatesSoft;
import com.github.zilosz.ssl.util.inventory.CustomInventory;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.Replacers;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerViewerInventory extends CustomInventory<Player>
    implements AutoUpdatesSoft, AutoUpdatesHard {

  @Override
  public List<Player> getItems() {
    return SSL
        .getInstance()
        .getGameManager()
        .getAlivePlayers()
        .stream()
        .sorted(Comparator.comparing(Player::getName))
        .collect(Collectors.toList());
  }

  @Override
  public String getTitle() {
    return "Player Viewer";
  }

  @Override
  public ItemStack getItemStack(Player clicker, Player other) {
    int lives = SSL.getInstance().getGameManager().getProfile(other).getLives();

    ItemBuilder<SkullMeta> itemBuilder = new ItemBuilder<SkullMeta>(Material.SKULL_ITEM)
        .setData((byte) 3)
        .setName(getPlayerName(other))
        .setCount(Math.max(1, lives));

    Skin skin = SSL.getInstance().getKitManager().getSelectedKit(other).getSkin();
    itemBuilder.applyMeta(skin::applyToSkull);

    List<String> description = List.of("&3&lHealth: &7{HEALTH}%");

    int health = (int) Math.ceil(100 * other.getHealth() / other.getMaxHealth());
    Replacers replacers = new Replacers().add("HEALTH", health);

    return itemBuilder.setLore(replacers.replaceLines(description)).get();
  }

  private String getPlayerName(Player player) {
    return SSL.getInstance().getTeamManager().getPlayerColor(player) + player.getName();
  }

  @Override
  public void onItemClick(
      InventoryContents contents, Player clicker, Player other, InventoryClickEvent event
  ) {
    if (other.getGameMode() == GameMode.SPECTATOR) {
      Chat.TRACKER.send(clicker, getPlayerName(other) + " &7is respawning...");
    }
    else {
      clicker.teleport(other);
      clicker.playSound(clicker.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
      Chat.TRACKER.send(clicker, "&7You teleported to " + getPlayerName(other) + "&7.");
    }
  }
}
