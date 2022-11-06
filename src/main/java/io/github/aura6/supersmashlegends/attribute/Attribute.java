package io.github.aura6.supersmashlegends.attribute;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class Attribute implements Listener {
    protected final SuperSmashLegends plugin;
    protected final Kit kit;
    protected Player player;

    public Attribute(SuperSmashLegends plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }

    public void activate() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    public void equip() {
        this.player = kit.getPlayer();
    }
}
