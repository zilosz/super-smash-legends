package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class LeftClickAbility extends ClickableAbility {

    public LeftClickAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || !event.getAction().name().contains("LEFT");
    }

    @Override
    public String getUseType() {
        return "Left Click";
    }
}
