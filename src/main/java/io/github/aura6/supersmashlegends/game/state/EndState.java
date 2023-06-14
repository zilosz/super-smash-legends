package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.database.PlayerDatabase;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.game.GameResult;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EndState extends GameState implements TeleportsOnVoid {
    private String winnerString;
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
    public boolean allowKitSelection() {
        return false;
    }

    @Override
    public boolean updatesKitSkins() {
        return false;
    }

    @Override
    public boolean allowsDamage() {
        return false;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        List<String> lines = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&f&lStatus",
                "&7Ending the game...",
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

        if (this.plugin.getTeamManager().doesPlayerHaveTeam(player)) {
            lines.add("");
            lines.add("&f&lKit");
            lines.add("{KIT}");
            replacers.add("KIT", this.plugin.getGameManager().getProfile(player).getKit().getBoldedDisplayName());
        }

        lines.add("&5&l---------------------");
        return replacers.replaceLines(lines);
    }

    @Override
    public void start() {
        this.plugin.getDamageManager().reset();

        GameManager gameManager = this.plugin.getGameManager();
        TeamManager teamManager = this.plugin.getTeamManager();

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

            if (teamManager.getTeamSize() == 1) {
                Player winner = winningSet.iterator().next();
                Kit winnerKit = this.plugin.getKitManager().getSelectedKit(winner);
                this.winnerString = winnerKit.getColor() + winner.getName();

            } else {
                this.winnerString = winningTeam.getColor() + winningTeam.getName();
            }

            for (Team team : teams) {

                for (Player player : team.getPlayers()) {

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
            Set<Player> tiedPlayers = rankedTeams.get(0).stream()
                    .flatMap(team -> team.getPlayers().stream()).collect(Collectors.toSet());

            for (Team team : teams) {

                for (Player player : team.getPlayers()) {
                    String tieString;
                    GameResult result;

                    if (tiedPlayers.contains(player)) {
                        result = GameResult.TIE;
                        tieString = "&7You have &e&ltied.";

                    } else {
                        result = GameResult.LOSE;

                        if (teamManager.getTeamSize() == 1) {
                            tieString = "&7There has been a &etie.";

                        } else {
                            tieString = "&7There has been a &etie &7between teams.";
                        }
                    }

                    Chat.GAME.send(player, tieString);
                    gameManager.getProfile(player).setGameResult(result);
                }
            }
        }

        Set<String> altNames = Set.of("AGentleAura", "EzRizzz", "TheBellahante");
        long altsUsed = playerRanks.keySet().stream().filter(p -> altNames.contains(p.getName())).count();

        for (Player player : playerRanks.keySet()) {
            gameManager.getProfile(player).getKit().destroy();

            UUID uuid = player.getUniqueId();
            InGameProfile profile = gameManager.getProfile(player);
            PlayerDatabase db = this.plugin.getPlayerDatabase();

            if (playerRanks.size() > 1 && altsUsed < 2) {
                db.increment(uuid, profile.getGameResult().getDbString(), 1);
                db.increment(uuid, "result." + playerRanks.get(player), 1);
                db.increment(uuid, "kills", profile.getKills());
                db.increment(uuid, "deaths", profile.getDeaths());
                db.increment(uuid, "damageDealt", profile.getDamageDealt());
                db.increment(uuid, "damageTaken", profile.getDamageTaken());
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ranking.forEach(line -> player.sendMessage(MessageUtils.color(line)));
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 2, 1);
            player.setAllowFlight(true);
        }

        this.endCountdown = new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.EndWaitSeconds");
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

                this.pitch += 1.5 / this.secondsLeft;
                this.secondsLeft--;
            }

        }.runTaskTimer(this.plugin, 100, 20);
    }

    @Override
    public void end() {
        this.winnerString = null;
        this.endCountdown.cancel();

        this.plugin.getTeamManager().reset();
        this.plugin.getWorldManager().resetWorld("arena");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            TitleAPI.clearTitle(player);
        }
    }

    @Override
    public Location getTeleportLocation() {
        return this.plugin.getArenaManager().getArena().getWaitLocation();
    }
}
