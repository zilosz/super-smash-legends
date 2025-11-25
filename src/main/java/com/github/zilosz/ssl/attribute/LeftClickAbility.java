package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class LeftClickAbility extends ClickableAbility {

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || !event.getAction().name().contains("LEFT");
  }

  @Override
  public String getUseType() {
    return "Left Click";
  }
}
