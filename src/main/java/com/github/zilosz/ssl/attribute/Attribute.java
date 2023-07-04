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
    private BukkitTask task;
    private boolean activated = false;

    public void initAttribute(Kit kit) {
        this.kit = kit;
    }

    public void activate() {
        this.activated = true;
        Bukkit.getPluginManager().registerEvents(this, SSL.getInstance());
        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), this::run, 0, this.period);
    }

    public void run() {}

    public void equip() {
        this.player = this.kit.getPlayer();
    }

    public void destroy() {
        this.unequip();

        if (this.activated) {
            this.deactivate();
        }
    }

    public void deactivate() {
        this.activated = false;
        HandlerList.unregisterAll(this);

        if (this.task != null) {
            this.task.cancel();
        }
    }

    public void unequip() {}
}
