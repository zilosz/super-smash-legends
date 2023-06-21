package com.github.zilosz.ssl.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import org.apache.commons.lang.StringUtils;
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
    protected final SSL plugin;

    public GameState(SSL plugin) {
        this.plugin = plugin;
    }

    public abstract String getConfigName();

    public abstract boolean allowKitSelection();

    public abstract boolean updatesKitSkins();

    public abstract boolean allowSpecCommand();

    public abstract List<String> getScoreboard(Player player);

    protected String getScoreboardLine() {
        int width = this.plugin.getResources().getConfig().getInt("Scoreboard.Width");
        return "&5&l" + StringUtils.repeat("-", width);
    }

    public abstract void start();

    public abstract void end();

    public boolean isSame(GameState other) {
        return this.getConfigName().equals(other.getConfigName());
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= this.plugin.getTeamManager().getAbsolutePlayerCap()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The game is full!");
        }
    }

    @EventHandler
    public void onGeneralJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.isInArena()) {
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined mid-game.", player.getName())));
            Chat.GAME.send(player, "&7The game you joined is in progress.");
            this.plugin.getGameManager().addSpectator(event.getPlayer());
            player.teleport(this.plugin.getArenaManager().getArena().getWaitLocation());

        } else {
            event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            ActionBarAPI.sendActionBar(player, MessageUtils.color("&7Welcome to &5&lSuper Smash Legends!"), 60);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
        }, 5);
    }

    public abstract boolean isInArena();

    @EventHandler
    public void onGeneralQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = this.plugin.getGameManager();

        if (this.isInArena() && gameManager.isPlayerAlive(player)) {
            String color = this.plugin.getTeamManager().getPlayerColor(player);
            event.setQuitMessage(Chat.QUIT.get(String.format("%s &7has quit mid-game.", color + player.getName())));

            gameManager.getProfile(player).setLives(0);

            if (!(this instanceof EndState)) {
                this.plugin.getTeamManager().getPlayerTeam(player).setLifespan(gameManager.getTicksActive());

                if (gameManager.getAlivePlayers().size() <= 1) {
                    gameManager.skipToState(new EndState(this.plugin));
                }
            }

        } else {
            event.setQuitMessage(Chat.QUIT.get(String.format("&5%s &7has quit the game.", player.getName())));
        }

        this.plugin.getKitManager().wipePlayer(player);

        gameManager.removeSpectator(player);
        gameManager.removeFutureSpectator(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (!this.allowsDamage()) {
            event.setCancelled(true);
        }

        boolean isVoid = event.getCause() == EntityDamageEvent.DamageCause.VOID;

        if (isVoid && this instanceof TeleportsOnVoid && event.getEntity() instanceof Player) {
            event.getEntity().teleport(((TeleportsOnVoid) this).getTeleportLocation());
            ((Player) event.getEntity()).playSound(event.getEntity().getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
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
