package com.github.zilosz.ssl.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChargedRightClickBlockAbility extends ChargedRightClickAbility {

    public ChargedRightClickBlockAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || event.getClickedBlock() == null;
    }

    @Override
    public String getUseType() {
        return "Hold Right Click Block";
    }
}
