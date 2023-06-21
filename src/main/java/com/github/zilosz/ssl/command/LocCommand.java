package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.utils.math.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class LocCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)) return false;

        Location location = ((Entity) sender).getLocation();
        String world = location.getWorld().getName();
        DecimalFormat format = new DecimalFormat("#.#");
        String x = format.format(MathUtils.roundToHalf(location.getX()));
        String y = format.format(MathUtils.roundToHalf(location.getY()));
        String z = format.format(MathUtils.roundToHalf(location.getZ()));
        String yaw = format.format(MathUtils.roundToHalf(location.getYaw()));
        Bukkit.broadcastMessage(String.format("%s:%s:%s:%s:%s:0", world, x, y, z, yaw));

        return true;
    }
}
