package io.github.zilosz.newsmashplugin.game;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import io.github.zilosz.newsmashplugin.game.state.GameState;
import io.github.zilosz.newsmashplugin.utils.message.MessageUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameScoreboard implements AssembleAdapter {
    private final GameStateManager gameStateManager;
    private final String title;
    private final Section statesSection;
    private final List<String> beforeContents;
    private final List<String> afterContents;

    public GameScoreboard(NewSmashPlugin plugin) {
        gameStateManager = plugin.getGame().getGameStateManager();
        Section scoreboardSection = plugin.getMessageConfig().getSection("scoreboard");
        title = MessageUtils.parse(scoreboardSection.getString("title"));
        Section contentsSection = scoreboardSection.getSection("contents");
        statesSection = contentsSection.getSection("states");
        beforeContents = MessageUtils.parse(contentsSection.getStringList("before"));
        afterContents = MessageUtils.parse(contentsSection.getStringList("after"));
    }

    @Override
    public String getTitle(Player player) {
        return title;
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>(beforeContents);
        GameState gameState = gameStateManager.getGameState();
        lines.addAll(gameState.parseScoreboardMessage(statesSection.getStringList(gameState.getSectionName())));
        lines.addAll(afterContents);
        return lines;
    }
}
