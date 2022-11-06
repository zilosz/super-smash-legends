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

    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.getInventory().setItem(slot, itemStack);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        player.getInventory().setItem(slot, null);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == player && event.getPlayer().getInventory().getHeldItemSlot() == slot && action != null) {
            action.accept(event);
        }
    }
}
