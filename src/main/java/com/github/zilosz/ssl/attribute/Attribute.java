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
    protected final SSL plugin;
    @Getter protected final Kit kit;
    @Getter protected Player player;
    private BukkitTask task;
    protected int period;
    @Getter private boolean enabled;

    public Attribute(SSL plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }

    public void activate() {
        if (this.enabled) return;

        this.enabled = true;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::run, 0, this.period);
    }

    public void deactivate() {
        if (!this.enabled) return;

        this.enabled = false;
        HandlerList.unregisterAll(this);

        if (this.task != null) {
            this.task.cancel();
        }
    }

    public void equip() {
        this.player = this.kit.getPlayer();
    }

    public void unequip() {}

    public void destroy() {
        this.deactivate();
        this.unequip();
    }

    public void run() {}
}
