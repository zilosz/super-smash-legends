package com.github.zilosz.ssl.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.function.Consumer;

public class HotbarItem implements Listener {
    private static final String[] ARMOR_KEYWORDS = {"BOOTS", "CHESTPLATE", "LEGGINGS", "HELMET"};

    @Getter private final Player player;
    @Getter private final ItemStack itemStack;
    @Getter private final int slot;

    @Setter private Consumer<PlayerInteractEvent> action;

    private Long lastTick;
    private Action lastAction;

    public HotbarItem(Player player, ItemStack itemStack, int slot) {
        this.player = player;
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.show();
    }

    public void show() {
        this.player.getInventory().setItem(this.slot, this.itemStack);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        this.hide();
    }

    public void hide() {
        this.player.getInventory().setItem(this.slot, null);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != this.player) return;
        if (event.getPlayer().getInventory().getHeldItemSlot() != this.slot) return;

        long tick = event.getPlayer().getWorld().getFullTime();
        Action action = event.getAction();

        if (this.lastTick != null && this.lastTick == tick) {
            boolean lastIsLeft = this.lastAction != null && this.lastAction.name().contains("LEFT");
            boolean currIsLeft = action.name().contains("LEFT");

            if (lastIsLeft == currIsLeft) return;
        }

        this.lastTick = tick;
        this.lastAction = action;

        Optional.ofNullable(event.getItem()).ifPresent(item -> {

            if (this.action != null) {
                this.action.accept(event);
            }

            String name = item.getType().name();

            for (String keyword : ARMOR_KEYWORDS) {

                if (name.contains(keyword)) {
                    event.setCancelled(true);
                    event.getPlayer().updateInventory();
                    break;
                }
            }
        });
    }
}
