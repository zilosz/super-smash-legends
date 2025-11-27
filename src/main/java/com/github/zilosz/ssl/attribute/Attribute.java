package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class Attribute implements Listener {
  @Getter protected Kit kit;
  @Getter protected Player player;
  protected int period;
  private BukkitTask ticker;

  public void assignKit(Kit kit) {
    this.kit = kit;
  }

  public void activate() {
    Bukkit.getPluginManager().registerEvents(this, SSL.getInstance());
    ticker = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), this::run, 0, period);
  }

  public void run() {}

  public void equip() {
    player = kit.getPlayer();
  }

  public void destroy() {
    unequip();
    deactivate();
  }

  public void unequip() {}

  public void deactivate() {
    HandlerList.unregisterAll(this);

    if (ticker != null) {
      ticker.cancel();
    }
  }
}
