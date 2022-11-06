package io.github.aura6.supersmashlegends.game.state;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.event.PlayerFinishTutorialEvent;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class TutorialState extends GameState {
    private int playersStarted;
    private int playersFinished = 0;

    public TutorialState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "Tutorial";
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
            plugin.getArenaManager().getArena().startTutorial(player);
            playersStarted++;
        }
    }

    @EventHandler
    public void onTutorialFinish(PlayerFinishTutorialEvent event) {
        if (++playersFinished >= playersStarted) {
            plugin.getStateManager().changeState(new PreGameState(plugin));
        }
    }
}
