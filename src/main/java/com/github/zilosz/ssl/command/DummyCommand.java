package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.util.message.Chat;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashSet;
import java.util.Set;

public class DummyCommand implements CommandExecutor, Listener {
  private final Set<LivingEntity> dummies = new HashSet<>();

  @Override
  public boolean onCommand(
      CommandSender commandSender, Command command, String s, String[] strings
  ) {
    if (!(commandSender instanceof Player)) return false;

    Entity player = (Entity) commandSender;
    EntityType type = EntityType.ZOMBIE;
    double health = 1_000;

    if (strings.length > 0) {

      if (NumberUtils.isNumber(strings[0])) {
        health = Double.parseDouble(strings[0]);
      }
      else {

        try {
          type = EntityType.valueOf(strings[0].toUpperCase());

        }
        catch (IllegalArgumentException e) {
          Chat.COMMAND.send(commandSender, "&7Invalid entity type.");
          return false;
        }

        if (strings.length == 2 && NumberUtils.isNumber(strings[1])) {
          health = Double.parseDouble(strings[1]);
        }
      }
    }

    LivingEntity dummy =
        (LivingEntity) player.getWorld().spawnEntity(player.getLocation().add(0, 60, 0), type);
    dummy.setMaxHealth(health);
    dummy.setHealth(health);
    dummies.add(dummy);

    return true;
  }

  @EventHandler
  public void onTarget(EntityTargetEvent event) {
    if (event.getEntity() instanceof LivingEntity &&
        dummies.contains((LivingEntity) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    dummies.remove(event.getEntity());
  }
}
