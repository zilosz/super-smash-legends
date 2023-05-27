package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
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
    public boolean isInArena() {
        return true;
    }

    @Override
    public List<String> getScoreboard(Player player) {
        List<String> winners = new ArrayList<>();

        for (Player winner : winningPlayers) {
            TeamManager teamManager = plugin.getTeamManager();
            String color = teamManager.getTeamSize() == 1 ? "&7" : teamManager.getPlayerTeam(winner).getColor();
            winners.add(color + winner.getName());
        }

        Replacers replacers = new Replacers().add("WINNERS", winners);

        List<String> lines = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&7Ending the game...",
                "",
                "&fWinners:",
                "{WINNERS}"
        ));

        if (this.plugin.getGameManager().isPlayerParticipating(player)) {
            lines.add("");
            replacers.add("KILLS", String.valueOf(plugin.getGameManager().getProfile(player).getKills()));
            replacers.add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
            lines.addAll(Arrays.asList("&fKills: &5{KILLS}", "&fKit: {KIT}"));
        }

        lines.add("&5&l---------------------");
        return replacers.replaceLines(lines);
    }

    @Override
    public void start() {
        this.plugin.getPowerManager().stop();

        TeamManager teamManager = this.plugin.getTeamManager();
        List<Team> winningTeams = MathUtils.findByHighestInt(teamManager.getTeamList(), Team::getLifespan);

        for (Team team: winningTeams) {
            this.winningPlayers.addAll(team.getPlayers());

            for (Player player : team.getPlayers()) {
                this.plugin.getGameManager().getProfile(player).setWinner(true);
            }
        }

        StringBuilder winners = new StringBuilder("&7");

        if (teamManager.getTeamSize() == 1) {
            winners.append(this.winningPlayers.stream().map(Player::getName).collect(Collectors.joining("&7, ")));

        } else {
            winners.append(this.winningPlayers.stream()
                    .map(player -> teamManager.getPlayerTeam(player).getColor() + player.getName())
                    .collect(Collectors.joining("&7, ")));
        }

        String title;

        if (winningTeams.size() == 1) {
            title = winningTeams.get(0).getSize() == 1 ? "&aWinner!" : "&aWinners!";

        } else {
            title = "&dTie!";
        }

        for (Player player : this.plugin.getGameManager().getParticipators()) {
            if (!player.isOnline()) return;

            String winMessage;

            if (this.winningPlayers.contains(player)) {

                if (winningTeams.size() == 1) {
                    winMessage = "&7You have &a&lwon!";

                } else {
                    winMessage = "&7You have &e&ltied.";
                }

            } else {
                winMessage = "&7You have &c&llost...";
            }

            Chat.GAME.send(player, winMessage);

            this.plugin.getKitManager().getSelectedKit(player).destroy();
            player.setAllowFlight(true);

            TitleAPI.sendTitle(player, title, MessageUtils.color(winners.toString()), 10, 40, 10);
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 3, 1);

            if (this.plugin.getGameManager().isPlayerParticipating(player)) {
                this.plugin.getGameManager().uploadPlayerStatsAtEnd(player, this.winningPlayers.contains(player));
            }
        }

        this.endCountdown = new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.EndWaitSeconds");
            float pitch = 0.5f;
            final double pitchStep = 1.5 / secondsLeft;

            @Override
            public void run() {

                if (this.secondsLeft == 0) {
                    plugin.getGameManager().advanceState();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    String title = MessageUtils.color("&7Resetting in...");
                    String subtitle = MessageUtils.color("&5&l" + this.secondsLeft);
                    TitleAPI.sendTitle(player, title, subtitle, 4, 12, 4);
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_HIT, 2, this.pitch);
                }

                this.pitch += this.pitchStep;
                this.secondsLeft--;
            }

        }.runTaskTimer(plugin, 70, 20);
    }

    @Override
    public void end() {
        endCountdown.cancel();
        winningPlayers.clear();

        plugin.getTeamManager().reset();
        plugin.getWorldManager().resetWorld("arena");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
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
