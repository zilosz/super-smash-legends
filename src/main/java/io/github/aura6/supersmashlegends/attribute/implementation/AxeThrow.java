package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.event.player.PlayerInteractEvent;

public class AxeThrow extends RightClickAbility {

    public AxeThrow(SuperSmashLegends plugin, Section config, Kit kit, int slot) {
        super(plugin, config, kit, slot);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {

    }
}
