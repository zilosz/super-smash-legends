package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
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
    public List<String> getScoreboard(Player player) {
        Arena arena = plugin.getArenaManager().getArena();

        Replacers replacers = new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors());

        List<String> lines = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&7The game is starting...",
                "",
                "&fArena: {ARENA}",
                "&fAuthors: &7{AUTHORS}"
        ));

        if (!plugin.getGameManager().isSpectator(player)) {
            lines.add("");
            lines.add(6, "&fKit: &5{KIT}");
            replacers.add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
        }

        lines.add("&5&l---------------------");
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
                    TitleAPI.sendTitle(player, MessageUtils.colorLines("&7Starting in..."), MessageUtils.colorLines("&5&l" + secondsLeft), 4, 12, 4);
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

    @EventHandler
    public void handleVoid(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().teleport(plugin.getArenaManager().getArena().getWaitLocation());
        }
    }
}
