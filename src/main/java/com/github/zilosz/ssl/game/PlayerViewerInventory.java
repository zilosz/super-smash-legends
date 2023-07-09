package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.Skin;
import com.github.zilosz.ssl.utils.inventory.CustomInventory;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.Replacers;
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

public class PlayerViewerInventory extends CustomInventory<Player> {

    @Override
    public String getTitle() {
        return "Player Viewer";
    }

    @Override
    public List<Player> getItems() {
        return SSL.getInstance().getGameManager().getAlivePlayers().stream()
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack(Player clicker, Player other) {
        int lives = SSL.getInstance().getGameManager().getProfile(other).getLives();

        ItemBuilder<SkullMeta> itemBuilder = new ItemBuilder<SkullMeta>(Material.SKULL_ITEM)
                .setData(3)
                .setName(this.getPlayerName(other))
                .setCount(Math.max(1, lives));

        Skin skin = SSL.getInstance().getKitManager().getSelectedKit(other).getSkin();
        itemBuilder.applyMeta(skin::applyToSkull);

        List<String> description = List.of("&3&lHealth: &7{HEALTH}%");

        Replacers replacers = new Replacers()
                .add("HEALTH", (int) Math.ceil(100 * other.getHealth() / other.getMaxHealth()));

        return itemBuilder.setLore(replacers.replaceLines(description)).get();
    }

    private String getPlayerName(Player player) {
        return SSL.getInstance().getTeamManager().getPlayerColor(player) + player.getName();
    }

    @Override
    public void onItemClick(Player clicker, Player other, InventoryClickEvent event) {

        if (other.getGameMode() == GameMode.SPECTATOR) {
            Chat.TRACKER.send(clicker, this.getPlayerName(other) + " &7is respawning...");

        } else {
            clicker.teleport(other);
            clicker.playSound(clicker.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            Chat.TRACKER.send(clicker, "&7You teleported to " + this.getPlayerName(other) + "&7.");
        }
    }

    @Override
    public boolean updatesItems() {
        return true;
    }
}
