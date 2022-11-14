package io.github.aura6.supersmashlegends.attribute;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class Attribute implements Listener {
    protected final SuperSmashLegends plugin;
    protected final Kit kit;
    @Getter protected Player player;
    private BukkitTask task;
    @Getter private boolean enabled;

    public Attribute(SuperSmashLegends plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }

    public void activate() {
        enabled = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0, 0);
    }

    public void deactivate() {
        enabled = false;
        HandlerList.unregisterAll(this);

        if (task != null) {
            task.cancel();
        }
    }

    public void equip() {
        player = kit.getPlayer();
    }

    public void unequip() {}

    public void destroy() {
        deactivate();
        unequip();
    }

    public void run() {}
}
