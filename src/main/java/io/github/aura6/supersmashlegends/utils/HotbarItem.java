package io.github.aura6.supersmashlegends.utils;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class HotbarItem implements Listener {
    private final Player player;
    private final ItemStack itemStack;
    private final int slot;
    @Setter private Consumer<PlayerInteractEvent> action;

    public HotbarItem(Player player, ItemStack itemStack, int slot) {
        this.player = player;
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public void show() {
        player.getInventory().setItem(slot, itemStack);
    }

    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        show();
    }

    public void hide() {
        player.getInventory().setItem(slot, null);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        hide();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player) return;
        if (event.getPlayer().getInventory().getHeldItemSlot() != slot) return;

        if (action != null) {
            action.accept(event);
        }

        String name = event.getItem().getType().name();

        if (name.contains("BOOTS") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("HELMET")) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }
}
