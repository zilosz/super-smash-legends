package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class InGameState extends GameState {

    public InGameState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "InGame";
    }

    @Override
    public Replacers getScoreboardReplacers(Player player) {
        return new Replacers();
    }

    @Override
    public void start() {
        super.start();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.WOLF_HOWL, 2, 0.8f);
            TitleAPI.sendTitle(player, MessageUtils.color("&7The &5game &7has started!"), "", 5, 30, 5);
            plugin.getKitManager().getSelectedKit(player).activate();
        }

        plugin.getArenaManager().getArena().teleportAllOnStart();
    }
}
