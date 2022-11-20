package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.database.Database;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import io.github.aura6.supersmashlegends.utils.message.Chat;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class EndState extends GameState {
    private final List<Player> winningPlayers = new ArrayList<>();
    private BukkitTask endCountdown;

    public EndState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "End";
    }

    @Override
    public List<String> getScoreboard(Player player) {
        List<String> winners = new ArrayList<>();

        for (Player winner : winningPlayers) {
            TeamManager teamManager = plugin.getTeamManager();
            String color = teamManager.getTeamSize() == 1 ? "&7" : teamManager.getPlayerTeam(winner).getColor();
            winners.add(color + winner.getName());
        }

        Replacers replacers = new Replacers()
                .add("WINNERS", winners)
                .add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());

        if (!plugin.getGameManager().isSpectator(player)) {
            replacers.add("KILLS", String.valueOf(plugin.getGameManager().getProfile(player).getKills()));
        }

        return replacers.replaceLines(Arrays.asList(
                "&5&l---------------------",
                "&7Ending the game...",
                "",
                "&fWinners:",
                "{WINNERS}",
                "",
                "&fKills: &5{KILLS}",
                "&fKit: {KIT}",
                "&5&l---------------------"
        ));
    }

    @Override
    public boolean isNotInGame() {
        return true;
    }

    @Override
    public void start() {
        plugin.getPowerManager().reset();

        TeamManager teamManager = plugin.getTeamManager();
        List<Team> winningTeams = MathUtils.findByHighestInt(teamManager.getTeams(), Team::getLifespan);

        for (Team team: winningTeams) {
            winningPlayers.addAll(team.getPlayers());

            for (Player player : team.getPlayers()) {
                plugin.getGameManager().getProfile(player).setWinner(true);
            }
        }

        StringBuilder winners = new StringBuilder("&7");

        if (teamManager.getTeamSize() == 1) {
            winners.append(winningPlayers.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining("&7, ")));

        } else {
            winners.append(winningPlayers.stream()
                    .map(player -> teamManager.getPlayerTeam(player).getColor() + player.getName())
                    .collect(Collectors.joining("&7, ")));
        }

        Section jewelsEarnedWeights = plugin.getResources().getConfig().getSection("Economy.JewelsEarnedWeights");
        int perKill = jewelsEarnedWeights.getInt("PerKill");

        String title = MessageUtils.color(winningTeams.size() == 1 ? "&aWinners!" : "&dTie!");

        for (Player player : plugin.getGameManager().getParticipators()) {
            String winMessage;

            if (winningPlayers.contains(player)) {

                if (winningTeams.size() == 1) {
                    winMessage = "&7You have &a&lwon!";

                } else {
                    winMessage = "&7You have &e&ltied.";
                }

            } else {
                winMessage = "&7You have &c&llost...";
            }

            Chat.GAME.send(player, winMessage);

            InGameProfile profile = plugin.getGameManager().getProfile(player);
            int jewelsEarned = profile.getKills() * perKill;

            if (winningPlayers.contains(player)) {
                jewelsEarned += jewelsEarnedWeights.getInt("WinBonus");
            }

            profile.setJewelsEarned(jewelsEarned);
            Chat.ECONOMY.send(player, String.format("&7You earned &f&l%d &7jewels.", jewelsEarned));

            plugin.getKitManager().getSelectedKit(player).destroy();
            player.setAllowFlight(true);

            TitleAPI.sendTitle(player, title, MessageUtils.color(winners.toString()), 10, 40, 10);
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 3, 1);
        }

        endCountdown = new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.EndWaitSeconds");
            float pitch = 0.5f;
            final double pitchStep = 1.5 / secondsLeft;

            @Override
            public void run() {

                if (secondsLeft == 0) {
                    plugin.getGameManager().advanceState();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(player, MessageUtils.color("&7Resetting in..."), MessageUtils.color("&5&l" + secondsLeft), 4, 12, 4);
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_HIT, 2, pitch);
                }

                pitch += pitchStep;
                secondsLeft--;
            }

        }.runTaskTimer(plugin, 70, 20);
    }

    @Override
    public void end() {
        endCountdown.cancel();

        plugin.getTeamManager().reset();
        plugin.getWorldManager().resetWorld("arena");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            TitleAPI.clearTitle(player);
        }

        for (Player player : plugin.getGameManager().getParticipators()) {
            InGameProfile profile = plugin.getGameManager().getProfile(player);
            UUID uuid = player.getUniqueId();
            Database db = plugin.getDb();

            db.setIfEnabled(uuid, "kills", db.getOrDefault(uuid, "kills", 0, 0) + profile.getKills());
            db.setIfEnabled(uuid, "deaths", db.getOrDefault(uuid, "deaths", 0, 0) + profile.getDeaths());
            db.setIfEnabled(uuid, "damageTaken", db.getOrDefault(uuid, "damageTaken", 0.0, 0.0) + profile.getDamageTaken());
            db.setIfEnabled(uuid, "damageDealt", db.getOrDefault(uuid, "damageDealt", 0.0, 0.0) + profile.getDamageDealt());

            if (winningPlayers.contains(player)) {
                db.setIfEnabled(uuid, "wins", db.getOrDefault(uuid, "wins", 0, 0) + 1);

            } else {
                db.setIfEnabled(uuid, "losses", db.getOrDefault(uuid, "losses", 0, 0) + 1);
            }
        }

        winningPlayers.clear();
    }

    @EventHandler
    public void handleVoid(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().teleport(plugin.getArenaManager().getArena().getWaitLocation());
        }
    }
}
