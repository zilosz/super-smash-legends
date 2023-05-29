package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public abstract class GameState implements Listener {
    protected final SuperSmashLegends plugin;

    public GameState(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public abstract String getConfigName();

    public abstract boolean isInArena();

    public abstract List<String> getScoreboard(Player player);

    public abstract void start();

    public abstract void end();

    public boolean isSame(GameState other) {
        return getConfigName().equals(other.getConfigName());
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= plugin.getTeamManager().getAbsolutePlayerCap()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The game is full!");
        }
    }

    @EventHandler
    public void onGeneralJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (isInArena()) {
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined mid-game.", player.getName())));
            Chat.GAME.send(player, "&7The game you joined is in progress.");
            this.plugin.getGameManager().addSpectator(event.getPlayer());
            player.teleport(this.plugin.getArenaManager().getArena().getWaitLocation());

        } else {
            Chat.GAME.send(player, "&7You can use &d&l/start &7 and &3&l/end &7to force game progress.");
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        }

        if (!player.hasPlayedBefore()) {
            String title = MessageUtils.color("&7Welcome to");
            String subtitle = MessageUtils.color("&5&lSuper Smash Legends!");
            TitleAPI.sendTitle(player, title, subtitle, 10, 40, 10);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
        }
    }

    @EventHandler
    public void onGeneralQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isInArena() && this.plugin.getGameManager().isPlayerAlive(player)) {
            String color = this.plugin.getTeamManager().getPlayerColor(player);
            event.setQuitMessage(Chat.QUIT.get(String.format("%s &7has quit mid-game.", color + player.getName())));

            if (!(this instanceof EndState)) {
                int lifespan = this.plugin.getGameManager().getTicksActive();
                this.plugin.getTeamManager().getPlayerTeam(player).setLifespan(lifespan);

                if (this.plugin.getGameManager().getAlivePlayers().size() <= 2) {
                    this.plugin.getGameManager().skipToState(new EndState(this.plugin));
                }
            }

        } else {
            event.setQuitMessage(Chat.QUIT.get(String.format("&5%s &7has quit the game.", player.getName())));
        }

        this.plugin.getKitManager().wipePlayer(player);
        this.plugin.getGameManager().removeSpectator(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(MessageUtils.color("&9" + event.getPlayer().getDisplayName() + ">> &7" + event.getMessage()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL || !(this instanceof InGameState)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
            case NATURAL: case EGG:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
