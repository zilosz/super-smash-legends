package io.github.aura6.supersmashlegends.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

public class DummyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;
        LivingEntity dummy = player.getWorld().spawn(player.getLocation().add(0, 60, 0), Zombie.class);
        dummy.setMaxHealth(200);
        dummy.setHealth(200);

        return false;
    }
}
