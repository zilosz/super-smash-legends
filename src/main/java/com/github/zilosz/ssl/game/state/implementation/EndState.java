package com.github.zilosz.ssl.game.state.implementation;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameResult;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import com.github.zilosz.ssl.util.message.Replacers;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EndState extends GameState {
    @Nullable private String winnerString;
    private BukkitTask endCountdown;

    @Override
    public boolean allowsSpecCommand() {
        return false;
    }

    @Override
    public boolean allowsKitSelection() {
        return false;
    }

    @Override
    public boolean updatesKitSkins() {
        return false;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        List<String> lines = new ArrayList<>(Arrays.asList(
                GameScoreboard.getLine(),
                "&f&lStatus",
                "&7Ending the game",
                ""
        ));

        Replacers replacers = new Replacers();

        if (this.winnerString == null) {
            lines.add("&f&lResult");
            lines.add("&eTie");

        } else {
            lines.add("&f&lWinner");
            lines.add("{WINNER}");
            replacers.add("WINNER", this.winnerString);
        }

        Optional.ofNullable(SSL.getInstance().getGameManager().getProfile(player)).ifPresent(profile -> {
            lines.add("");
            lines.add("&f&lKit");
            lines.add("{KIT}");
            replacers.add("KIT", profile.getKit().getDisplayName());
        });

        lines.add(GameScoreboard.getLine());
        return replacers.replaceLines(lines);
    }

    @Override
    public void start() {
        SSL.getInstance().getDamageManager().reset();

        GameManager gameManager = SSL.getInstance().getGameManager();
        TeamManager teamManager = SSL.getInstance().getTeamManager();

        teamManager.getAliveTeams().forEach(team -> team.setLifespan(gameManager.getTicksActive() + 1));
        Comparator<Team> comp = Comparator.comparingInt(Team::getLifespan).reversed();

        List<Team> teams = teamManager.getTeamList();
        List<List<Team>> rankedTeams = CollectionUtils.getRankedGroups(teams, comp);

        List<String> ranking = new ArrayList<>(Arrays.asList(
                "&5--------------------------",
                "&d&lFinal Player Ranking",
                ""
        ));

        Map<Player, Integer> playerRanks = new HashMap<>();
        int currRankIndex = 0;

        while (currRankIndex < Math.min(3, rankedTeams.size())) {
            StringBuilder players = new StringBuilder();

            for (Team team : rankedTeams.get(currRankIndex)) {

                for (Player player : team.getPlayers()) {
                    players.append(teamManager.getPlayerColor(player)).append(player.getName()).append("&7, ");
                    playerRanks.put(player, currRankIndex + 1);
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

        if (rankedTeams.get(0).size() == 1) {
            Team winningTeam = rankedTeams.get(0).get(0);
            Set<Player> winningSet = winningTeam.getPlayers();

            if (teamManager.isTeamsModeEnabled()) {
                this.winnerString = winningTeam.getName();

            } else {
                Player winner = winningSet.iterator().next();
                Kit winnerKit = SSL.getInstance().getKitManager().getSelectedKit(winner);
                this.winnerString = winnerKit.getColor().getChatSymbol() + winner.getName();
            }

            for (Team team : teams) {
                Set<Player> players = team.getPlayers();

                for (Player player : players) {

                    if (winningSet.contains(player)) {
                        gameManager.getProfile(player).setGameResult(GameResult.WIN);
                        Chat.GAME.send(player, "&7You have &a&lwon!");

                    } else {
                        gameManager.getProfile(player).setGameResult(GameResult.LOSE);
                        Chat.GAME.send(player, String.format("%s &7has &awon!", this.winnerString));
                    }
                }
            }

        } else {
            Set<Player> tiedPlayers = rankedTeams.get(0)
                    .stream()
                    .flatMap(team -> team.getPlayers().stream())
                    .collect(Collectors.toSet());

            for (Team team : teams) {
                Set<Player> players = team.getPlayers();

                for (Player player : players) {
                    String tieString;
                    GameResult result;

                    if (tiedPlayers.contains(player)) {
                        result = GameResult.TIE;
                        tieString = "&7You have &e&ltied.";

                    } else {
                        result = GameResult.LOSE;

                        if (teamManager.isTeamsModeEnabled()) {
                            tieString = "&7There has been a &etie &7between teams.";

                        } else {
                            tieString = "&7There has been a &etie.";
                        }
                    }

                    Chat.GAME.send(player, tieString);
                    gameManager.getProfile(player).setGameResult(result);
                }
            }
        }

        for (Player player : playerRanks.keySet()) {
            InGameProfile profile = gameManager.getProfile(player);
            profile.getKit().destroy();

            if (playerRanks.size() > 1) {
                PlayerDatabase playerDatabase = SSL.getInstance().getPlayerDatabase();
                profile.updatePlayerData(playerDatabase.getPlayerData(player));
                playerDatabase.savePlayerData(player);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ranking.forEach(line -> player.sendMessage(MessageUtils.color(line)));
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 2, 1);
            player.setAllowFlight(true);
        }

        this.endCountdown = new BukkitRunnable() {
            int secondsLeft = SSL.getInstance().getResources().getConfig().getInt("Game.EndWaitSeconds");
            float pitch = 0.5f;

            @Override
            public void run() {

                if (this.secondsLeft == 0) {
                    gameManager.advanceState();
                    return;
                }

                String message = MessageUtils.color("&7Resetting in &5&l" + this.secondsLeft + " &7seconds.");

                for (Player player : Bukkit.getOnlinePlayers()) {
                    ActionBarAPI.sendActionBar(player, message);
                    player.playSound(player.getLocation(), Sound.CLICK, 1, this.pitch);
                }

                this.pitch += 1.5f / this.secondsLeft;
                this.secondsLeft--;
            }

        }.runTaskTimer(SSL.getInstance(), 100, 20);
    }

    @Override
    public void end() {
        this.winnerString = null;
        this.endCountdown.cancel();

        SSL.getInstance().getWorldManager().resetWorld(CustomWorldType.ARENA);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            TitleAPI.clearTitle(player);
        }
    }

    @Override
    public boolean isInArena() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean allowsDamage() {
        return false;
    }

    @EventHandler
    public void onEndDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        }
    }
}
