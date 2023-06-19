package com.github.zilosz.ssl.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class RightClickBlockAbility extends RightClickAbility {

    public RightClickBlockAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || event.getClickedBlock() == null;
    }

    @Override
    public String getUseType() {
        return "Right Click Block";
    }
}
