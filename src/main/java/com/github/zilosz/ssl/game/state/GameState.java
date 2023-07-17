package com.github.zilosz.ssl.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    @Getter @Setter private GameStateType type;

    public abstract boolean allowsSpecCommand();

    public abstract boolean allowsKitSelection();

    public abstract boolean updatesKitSkins();

    public abstract List<String> getScoreboard(Player player);

    public abstract void start();

    public abstract void end();

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= SSL.getInstance().getTeamManager().getAbsolutePlayerCap()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The game is full!");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onGeneralJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.getInventory().clear();
        player.setHealth(20);
        player.setLevel(0);

        PlayerDatabase database = SSL.getInstance().getPlayerDatabase();
        database.set(player.getUniqueId(), "name", player.getName());

        if (this.isInArena()) {
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined mid-game.", player.getName())));
            Chat.GAME.send(player, "&7The game you joined is in progress.");

            player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());

            GameManager gameManager = SSL.getInstance().getGameManager();
            gameManager.getSpectators().forEach(player::hidePlayer);
            gameManager.addSpectator(player);

        } else {
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        }

        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            ActionBarAPI.sendActionBar(player, MessageUtils.color("&7Welcome to &5&lSuper Smash Legends!"), 60);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
        }, 5);
    }

    public abstract boolean isInArena();

    @EventHandler
    public void onGeneralQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = SSL.getInstance().getGameManager();

        if (this.isInArena() && gameManager.isPlayerAlive(player)) {
            String color = SSL.getInstance().getTeamManager().getPlayerColor(player);
            event.setQuitMessage(Chat.QUIT.get(String.format("%s &7has quit mid-game.", color + player.getName())));

            gameManager.getProfile(player).setLives(0);

            if (this.isPlaying()) {
                SSL.getInstance().getTeamManager().getEntityTeam(player).setLifespan(gameManager.getTicksActive());

                if (gameManager.getAlivePlayers().size() <= 1) {
                    gameManager.skipToState(GameStateType.END);
                }
            }

        } else {
            event.setQuitMessage(Chat.QUIT.get(String.format("&5%s &7has quit the game.", player.getName())));
        }

        SSL.getInstance().getKitManager().wipePlayer(player);

        gameManager.removeSpectator(player);
        gameManager.removeFutureSpectator(player);
    }

    public abstract boolean isPlaying();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onGeneralEntityDamage(EntityDamageEvent event) {
        if (!this.allowsDamage()) {
            event.setCancelled(true);
        }
    }

    public abstract boolean allowsDamage();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(MessageUtils.color("&9%s" + "» &7%s"));
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
            case NATURAL:
            case EGG:
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
