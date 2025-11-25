package com.github.zilosz.ssl.event.attribute;

import com.github.zilosz.ssl.attribute.ClickableAbility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class AbilityUseEvent extends Event implements Cancellable {
  private static final HandlerList HANDLERS = new HandlerList();

  private final ClickableAbility ability;
  @Setter private boolean cancelled;

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
