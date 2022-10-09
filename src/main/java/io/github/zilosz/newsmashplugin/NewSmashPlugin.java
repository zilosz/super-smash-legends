package io.github.zilosz.newsmashplugin;

import com.sk89q.worldedit.Vector;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.github.thatkawaiisam.assemble.Assemble;
import io.github.thatkawaiisam.assemble.AssembleStyle;
import io.github.zilosz.newsmashplugin.game.Game;
import io.github.zilosz.newsmashplugin.game.GameScoreboard;
import io.github.zilosz.newsmashplugin.utils.FileHelper;
import io.github.zilosz.newsmashplugin.utils.WorldMaker;
import io.github.zilosz.newsmashplugin.utils.YamlReader;
import io.github.zilosz.newsmashplugin.utils.message.Chatter;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class NewSmashPlugin extends JavaPlugin implements Listener {
    @Getter private Game game;
    @Getter private YamlDocument config;
    @Getter private YamlDocument messageConfig;
    @Getter private YamlDocument lobbyConfig;
    @Getter private Chatter chatter;

    @Override
    public void onLoad() {

        try {
            if (WorldMaker.delete("lobby")) {
                getLogger().log(Level.INFO, "Deleted the lobby world.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {

        try {
            config = FileHelper.loadYaml(this, "config");
            messageConfig = FileHelper.loadYaml(this, "messages");
            lobbyConfig = FileHelper.loadYaml(this, "lobby");

            Vector vector = YamlReader.readWEVector(lobbyConfig.getSection("spawnLocation"));
            WorldMaker.create("lobby", FileHelper.loadSchematic(this, "lobby"), vector);

        } catch (IOException e) {
            e.printStackTrace();
        }

        game = new Game(this);
        chatter = new Chatter(this);

        Assemble scoreboard = new Assemble(this, new GameScoreboard(this));
        scoreboard.setTicks(20);
        scoreboard.setAssembleStyle(AssembleStyle.MODERN);
    }
}
