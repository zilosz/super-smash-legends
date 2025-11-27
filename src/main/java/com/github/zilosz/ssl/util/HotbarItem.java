package com.github.zilosz.ssl.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

import java.util.function.Consumer;

@RequiredArgsConstructor
public class HotbarItem implements Listener {
  private static final String[] ARMOR_KEYWORDS = {"BOOTS", "CHESTPLATE", "LEGGINGS", "HELMET"};

  @Getter private final Player player;
  @Getter private final ItemStack itemStack;
  @Getter private final int slot;

  @Setter private Consumer<PlayerInteractEvent> action;

  private Long lastTick;
  private Action lastAction;

  public void registerAndShow(Plugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    show();
  }

  public void show() {
    player.getInventory().setItem(slot, itemStack);
  }

  public void destroy() {
    HandlerList.unregisterAll(this);
    hide();
  }

  public void hide() {
    player.getInventory().setItem(slot, null);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getPlayer() != player) return;
    if (event.getPlayer().getInventory().getHeldItemSlot() != slot) return;
    if (!itemStack.equals(event.getItem())) return;

    long tick = event.getPlayer().getWorld().getFullTime();
    Action action = event.getAction();

    boolean sameTick = lastTick != null && lastTick == tick;
    boolean lastIsLeft = lastAction != null && lastAction.name().contains("LEFT");
    boolean currIsLeft = action.name().contains("LEFT");

    if (sameTick && lastIsLeft == currIsLeft) return;

    lastTick = tick;
    lastAction = action;

    if (this.action != null) {
      this.action.accept(event);
    }

    String name = event.getItem().getType().name();

    for (String keyword : ARMOR_KEYWORDS) {

      if (name.contains(keyword)) {
        event.setCancelled(true);
        event.getPlayer().updateInventory();

        break;
      }
    }
  }
}
