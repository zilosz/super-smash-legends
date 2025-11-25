package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.util.message.Chat;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class HealCommand implements CommandExecutor {

  @Override
  public boolean onCommand(
      CommandSender commandSender, Command command, String s, String[] strings
  ) {
    if (strings.length == 0) return false;
    if (!NumberUtils.isNumber(strings[0])) return false;

    double amount = NumberUtils.createDouble(strings[0]);

    if (strings.length == 1) {

      if (commandSender instanceof Player) {
        heal((Player) commandSender, amount);
      }

      return true;
    }

    if (strings.length == 2) {
      Optional.ofNullable(Bukkit.getPlayer(strings[1])).ifPresent(player -> heal(player, amount));
      return true;
    }

    return false;
  }

  private void heal(Player player, double amount) {
    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + amount));
    Chat.COMMAND.send(player, "&7Healed for &e" + amount + " &7health.");
  }
}
