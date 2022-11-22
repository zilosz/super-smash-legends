package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Bukkit;
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

    public abstract List<String> getScoreboard(Player player);

    public abstract boolean isInGame();

    public abstract void start();

    public abstract void end();

    public boolean isSame(GameState other) {
        return getConfigName().equals(other.getConfigName());
    }

    @EventHandler
    public void handleJoinCapacity(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= plugin.getTeamManager().getPlayerCap()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The game is full!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        TitleAPI.sendTitle(player, MessageUtils.color("&7Welcome to"), MessageUtils.color("&5&lSuper Smash Legends!"), 10, 40, 10);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
        Chat.GAME.send(player, "&7Please use &d&l/start &7to start and &3&l/end &7to end.");

        if (isInGame()) {
            Chat.GAME.send(player, "&7The game you joined is in progress.");
            plugin.getGameManager().addSpectator(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getEconomyManager().uploadUser(player);
        plugin.getKitManager().uploadUser(player);

        if (!isInGame()) {
            event.setQuitMessage(Chat.QUIT.get(String.format("&5%s &7has quit the game.", player.getName())));
            return;
        }

        event.setQuitMessage(Chat.QUIT.get(String.format("&5%s &7has quit mid-game.", player.getName())));

        plugin.getKitManager().getSelectedKit(player).deactivate();

        GameManager gameManager = plugin.getGameManager();
        gameManager.removeSpectator(player);

        plugin.getTeamManager().wipePlayer(player);
        gameManager.wipePlayer(player);

        if (gameManager.getAlivePlayers().size() <= 1) {
            gameManager.advanceState();
        }
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
        event.setCancelled(true);
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
        event.setCancelled(true);
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
