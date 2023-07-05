package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public class ChargedRightClickBlockAbility extends ChargedRightClickAbility {

    @Override
    public String getUseType() {
        return "Hold Right Click Block";
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || event.getClickedBlock() == null;
    }
}
