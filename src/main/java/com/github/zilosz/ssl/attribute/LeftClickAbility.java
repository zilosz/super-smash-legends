package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class LeftClickAbility extends ClickableAbility {

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return !event.getAction().name().contains("LEFT") || super.invalidate(event);
  }

  @Override
  public String getUseType() {
    return "Left Click";
  }
}
