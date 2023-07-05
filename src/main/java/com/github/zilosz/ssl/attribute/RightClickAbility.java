package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class RightClickAbility extends ClickableAbility {

    @Override
    public String getUseType() {
        return "Right Click";
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || !event.getAction().name().contains("RIGHT");
    }
}
