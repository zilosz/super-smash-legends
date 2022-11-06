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

public class PreGameState extends GameState {

    public PreGameState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "PreGame";
    }

    @Override
    public Replacers getScoreboardReplacers(Player player) {
        Arena arena = plugin.getArenaManager().getArena();

        return new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors())
                .add("KIT", plugin.getKitManager().getSelectedKit(player).getDisplayName());
    }

    @Override
    public void start() {
        super.start();

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getArenaManager().getArena().teleportToWait(player);
            player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 2, 1);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.StartWaitSeconds");
            float pitch = 0.5f;
            final double pitchStep = 1.5 / secondsLeft;

            @Override
            public void run() {

                if (secondsLeft == 0) {
                    plugin.getStateManager().changeState(new InGameState(plugin));
                    cancel();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(player, MessageUtils.color("&7Starting in..."), MessageUtils.color("&5&l" + secondsLeft), 4, 12, 4);
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_HIT, 2, pitch);
                    pitch += pitchStep;
                }

                secondsLeft--;
            }

        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void end() {
        super.end();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void tpOnVoid(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            plugin.getArenaManager().getArena().teleportToWait((Player) event.getEntity());
        }
    }
}
