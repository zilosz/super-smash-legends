package io.github.aura6.supersmashlegends.attribute.implementation;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Bukkit;

public class Regeneration extends Attribute {

    public Regeneration(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
    }

    @Override
    public void run() {
        RegenEvent regenEvent = new RegenEvent(player, kit.getRegen());
        Bukkit.getPluginManager().callEvent(regenEvent);
        player.setHealth(Math.min(20, player.getHealth() + regenEvent.getRegen() / 20));
    }
}
