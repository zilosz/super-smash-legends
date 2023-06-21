package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.damage.DamageSettings;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.utils.message.Chat;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DamageCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return false;
        if (!NumberUtils.isNumber(strings[0])) return false;

        double damage = NumberUtils.createDouble(strings[0]);

        if (strings.length == 1) {

            if (commandSender instanceof Player) {
                damage(commandSender, (Player) commandSender, damage);
            }

            return true;

        } else if (strings.length == 2) {

            Optional.ofNullable(Bukkit.getPlayer(strings[1])).ifPresent(player -> {
                damage(commandSender, player, damage);
            });

            return true;
        }

        return false;
    }

    private static void damage(CommandSender sender, Player player, double damage) {
        DamageSettings settings = new DamageSettings(damage, false);
        DamageEvent event = new DamageEvent(player, settings, false);
        Bukkit.getPluginManager().callEvent(event);

        double finalDamage = event.getFinalDamage();
        player.damage(finalDamage);
        Chat.COMMAND.send(sender, String.format("&7Damaged &5%s &7for &e%f &7damage.", player.getName(), finalDamage));
    }
}
