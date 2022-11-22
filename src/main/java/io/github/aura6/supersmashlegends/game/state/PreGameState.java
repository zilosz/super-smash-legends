package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    public List<String> getScoreboard(Player player) {
        Arena arena = plugin.getArenaManager().getArena();

        Replacers replacers = new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors());

        List<String> lore = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&7The game is starting...",
                "",
                "&fArena: {ARENA}",
                "&fAuthors: &7{AUTHORS}",
                "",
                "&5&l---------------------"
        ));

        if (!plugin.getGameManager().isSpectator(player)) {
            lore.add(6, "&fKit: &5{KIT}");
            replacers.add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
        }

        return replacers.replaceLines(lore);
    }

    @Override
    public boolean isInGame() {
        return true;
    }

    @Override
    public void start() {
        Set<Player> players = plugin.getGameManager().getAlivePlayers();

        for (Player player : players) {
            player.teleport(plugin.getArenaManager().getArena().getWaitLocation());
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

                for (Player player : players) {
                    TitleAPI.sendTitle(player, MessageUtils.color("&7Starting in..."), MessageUtils.color("&5&l" + secondsLeft), 4, 12, 4);
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_HIT, 2, pitch);
                }

                pitch += pitchStep;
                secondsLeft--;
            }

        }.runTaskTimer(plugin, 40, 20);
    }

    @Override
    public void end() {
        startCountdown.cancel();

        for (Player player : plugin.getGameManager().getAlivePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            TitleAPI.clearTitle(player);
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
