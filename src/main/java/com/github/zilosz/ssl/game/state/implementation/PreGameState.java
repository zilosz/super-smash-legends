package com.github.zilosz.ssl.game.state.implementation;

import com.connorlinfoot.titleapi.TitleAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.arena.Arena;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import com.github.zilosz.ssl.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreGameState extends GameState {
    private BukkitTask startCountdown;

    @Override
    public boolean allowsSpecCommand() {
        return false;
    }

    @Override
    public boolean allowsKitSelection() {
        return true;
    }

    @Override
    public boolean updatesKitSkins() {
        return true;
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

        Arena arena = SSL.getInstance().getArenaManager().getArena();
        Replacers replacers = new Replacers().add("ARENA", arena.getName()).add("AUTHORS", arena.getAuthors());

        if (!SSL.getInstance().getGameManager().isSpectator(player)) {
            lines.add("");
            lines.add("&f&lKit");
            lines.add("{KIT}");
            replacers.add("KIT", SSL.getInstance().getKitManager().getSelectedKit(player).getDisplayName());
        }

        lines.add(this.getScoreboardLine());
        return replacers.replaceLines(lines);
    }

    @Override
    public void start() {

        for (Player player : SSL.getInstance().getGameManager().getAlivePlayers()) {
            player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        this.startCountdown = new BukkitRunnable() {
            int secondsLeft = SSL.getInstance().getResources().getConfig().getInt("Game.StartWaitSeconds");
            final double pitchStep = 1.5 / this.secondsLeft;
            float pitch = 0.5f;

            @Override
            public void run() {

                if (this.secondsLeft == 0) {
                    SSL.getInstance().getGameManager().advanceState();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    String title = MessageUtils.color("&7Starting in...");
                    TitleAPI.sendTitle(player, title, MessageUtils.color("&5&l" + this.secondsLeft), 4, 12, 4);

                    player.playSound(player.getLocation(), Sound.CLICK, 1, this.pitch);
                }

                this.pitch += this.pitchStep;
                this.secondsLeft--;
            }

        }.runTaskTimer(SSL.getInstance(), 40, 20);
    }

    @Override
    public void end() {
        this.startCountdown.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleAPI.clearTitle(player);

            if (SSL.getInstance().getGameManager().isPlayerAlive(player)) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }

    @Override
    public boolean isInArena() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return true;
    }

    @Override
    public boolean allowsDamage() {
        return false;
    }

    @EventHandler
    public void onPreGameDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        }
    }
}
