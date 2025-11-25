package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.message.Chat;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

@Setter
public class DjsPassion extends RightClickAbility {
  private boolean succeeded;

  @Override
  public void onClick(PlayerInteractEvent event) {

    if (succeeded) {
      startCooldown();
    }
    else {
      Chat.ABILITY.send(player, "&7There is no Boombox in sight...");
      player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
    }

    succeeded = false;
  }
}
