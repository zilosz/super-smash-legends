package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreGameState extends GameState implements TeleportsOnVoid {
    private BukkitTask startCountdown;

    public PreGameState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "PreGame";
    }

    @Override
    public boolean isInArena() {
        return true;
    }

    @Override
    public boolean allowKitSelection() {
        return true;
    }

    @Override
    public boolean updatesKitSkins() {
        return true;
    }

    @Override
    public boolean allowsDamage() {
        return false;
    }

    @Override
    public boolean allowSpecCommand() {
        return false;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        List<String> lines = new ArrayList<>(Arrays.asList(
                this.getScoreboardLine(),
                "&f&lStatus",
                "&7The game is starting",
                "",
                "&f&lArena",
                "{ARENA}",
                "",
                "&f&lAuthors",
                "&7{AUTHORS}"
        ));

        Arena arena = this.plugin.getArenaManager().getArena();

        Replacers replacers = new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors());

        if (!this.plugin.getGameManager().isSpectator(player)) {
            lines.add("");
            lines.add("&f&lKit");
            lines.add("{KIT}");
            replacers.add("KIT", this.plugin.getKitManager().getSelectedKit(player).getDisplayName());
        }

        lines.add(this.getScoreboardLine());
        return replacers.replaceLines(lines);
    }

    @Override
    public void start() {

        for (Player player : this.plugin.getGameManager().getAlivePlayers()) {
            player.teleport(this.plugin.getArenaManager().getArena().getWaitLocation());
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        startCountdown = new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.StartWaitSeconds");
            float pitch = 0.5f;
            final double pitchStep = 1.5 / secondsLeft;

            @Override
            public void run() {

                if (secondsLeft == 0) {
                    plugin.getGameManager().advanceState();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(player, MessageUtils.color("&7Starting in..."), MessageUtils.color("&5&l" + secondsLeft), 4, 12, 4);
                    player.playSound(player.getLocation(), Sound.CLICK, 1, pitch);
                }

                pitch += pitchStep;
                secondsLeft--;
            }

        }.runTaskTimer(plugin, 40, 20);
    }

    @Override
    public void end() {
        this.startCountdown.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleAPI.clearTitle(player);

            if (this.plugin.getGameManager().isPlayerAlive(player)) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }

    @Override
    public Location getTeleportLocation() {
        return this.plugin.getArenaManager().getArena().getWaitLocation();
    }
}
