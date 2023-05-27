package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EndState extends GameState {
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

        List<String> lines = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&7Ending the game..."
        ));

        Replacers replacers = new Replacers();

        if (this.plugin.getGameManager().isPlayerParticipating(player)) {
            lines.add("");
            replacers.add("KILLS", String.valueOf(this.plugin.getGameManager().getProfile(player).getKills()));
            replacers.add("KIT", this.plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
            lines.addAll(Arrays.asList("&fKills: &5{KILLS}", "&fKit: {KIT}"));
        }

        lines.add("&5&l---------------------");
        return replacers.replaceLines(lines);
    }

    private void broadcastWin(Team winningTeam, String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !winningTeam.getPlayers().contains(player))
                .forEach(player -> Chat.GAME.send(player, message));
    }

    @Override
    public void start() {
        this.plugin.getPowerManager().stop();

        TeamManager teamManager = this.plugin.getTeamManager();

        teamManager.getAliveTeams().forEach(team -> team.setLifespan(team.getLifespan() + 1));

        Comparator<Team> comp = Comparator.comparingInt(Team::getLifespan);
        List<List<Team>> rankedTeams = CollectionUtils.getRankedGroups(teamManager.getTeamList(), comp);

        List<Player> winningPlayers = new ArrayList<>();

        for (Team winningTeam : rankedTeams.get(0)) {

            for (Player player : winningTeam.getPlayers()) {
                this.plugin.getGameManager().getProfile(player).setWinner(true);
                winningPlayers.add(player);
            }
        }

        List<String> ranking = new ArrayList<>(Arrays.asList(
                "&5--------------------------",
                "&5&lFinal Player Ranking",
                ""
        ));

        int currRankIndex = 0;

        while (currRankIndex < Math.min(3, rankedTeams.size())) {
            StringBuilder players = new StringBuilder();

            for (Team team : rankedTeams.get(currRankIndex)) {

                for (Player player : team.getPlayers()) {
                    players.append(teamManager.getPlayerColor(player)).append(player.getName()).append("&7, ");
                }
            }

            if (players.toString().endsWith(", ")) {
                players.delete(players.length() - 2, players.length());
            }

            String rankColor;

            switch (currRankIndex) {

                case 0:
                    rankColor = "&a";
                    break;

                case 1:
                    rankColor = "&e";
                    break;

                case 2:
                    rankColor = "&6";
                    break;

                default:
                    rankColor = "&9";
            }

            ranking.add(String.format("%s&l%d. %s", rankColor, currRankIndex + 1, players));
            currRankIndex++;
        }

        ranking.add("&5--------------------------");

        StringBuilder winners = new StringBuilder("&7").append(winningPlayers.stream()
                .map(player -> teamManager.getPlayerColor(player) + player.getName())
                .collect(Collectors.joining("&7, ")));

        String title;

        if (rankedTeams.get(0).size() == 1) {
            title = teamManager.getTeamSize() == 1 ? "&aWinner!" : "&aWinners!";

        } else {
            title = "&dTie!";
        }

        for (Player player : this.plugin.getGameManager().getParticipators()) {
            if (!player.isOnline()) return;

            ranking.forEach(line -> player.sendMessage(MessageUtils.color(line)));

            String winMessage;

            if (winningPlayers.contains(player)) {

                if (rankedTeams.get(0).size() == 1) {
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
                this.plugin.getGameManager().uploadPlayerStatsAtEnd(player, winningPlayers.contains(player));
            }
        }

        Team winningTeam = rankedTeams.get(0).get(0);

        if (teamManager.getTeamSize() == 1) {

            if (rankedTeams.get(0).size() == 1) {
                Player winner = winningTeam.getPlayers().get(0);
                broadcastWin(winningTeam, teamManager.getPlayerColor(winner) + winner.getName() + " &7has won!");

            } else {
                Chat.GAME.broadcast("&7There has been a &e&ltie!");
            }

        } else if (rankedTeams.get(0).size() == 1) {
            broadcastWin(winningTeam, winningTeam.getColor() + winningTeam.getName() + " &7has won!");

        } else {
            Chat.GAME.broadcast("&7There has been a &e&ltie &7between teams!");
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

        plugin.getTeamManager().reset();
        plugin.getWorldManager().resetWorld("arena");

        for (Player player : Bukkit.getOnlinePlayers()) {
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
