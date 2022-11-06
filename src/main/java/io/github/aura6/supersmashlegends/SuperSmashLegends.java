package io.github.aura6.supersmashlegends;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import fr.minuskube.inv.InventoryManager;
import io.github.aura6.supersmashlegends.arena.ArenaManager;
import io.github.aura6.supersmashlegends.command.KitCommand;
import io.github.aura6.supersmashlegends.command.ReloadConfigCommand;
import io.github.aura6.supersmashlegends.command.StartCommand;
import io.github.aura6.supersmashlegends.database.Database;
import io.github.aura6.supersmashlegends.economy.EconomyManager;
import io.github.aura6.supersmashlegends.game.GameScoreboard;
import io.github.aura6.supersmashlegends.game.state.StateManager;
import io.github.aura6.supersmashlegends.inventory.ArenaVoter;
import io.github.aura6.supersmashlegends.inventory.KitSelector;
import io.github.aura6.supersmashlegends.inventory.TeamSelector;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.WorldMaker;
import io.github.aura6.supersmashlegends.utils.file.FileLoader;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.thatkawaiisam.assemble.Assemble;
import io.github.thatkawaiisam.assemble.AssembleStyle;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

public class SuperSmashLegends extends JavaPlugin {
    @Getter private Resources resources;
    @Getter private Database db;
    @Getter private EconomyManager economyManager;
    @Getter private KitManager kitManager;
    @Getter private InventoryManager inventoryManager;
    @Getter private KitSelector kitSelector;
    @Getter private ArenaVoter arenaVoter;
    @Getter private StateManager stateManager;
    @Getter private ArenaManager arenaManager;
    @Getter private TeamManager teamManager;
    @Getter private TeamSelector teamSelector;

    @Override
    public void onLoad() {
        WorldMaker.delete("lobby");
        WorldMaker.delete("arena");
    }

    @Override
    public void onEnable() {
        resources = new Resources(this);

        Vector pasteVector = YamlReader.readVector(resources.getLobby().getString("PasteVector"));
        File schematic = FileLoader.loadSchematic(this, "lobby");
        WorldMaker.create("lobby", schematic, pasteVector);

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        teamManager = new TeamManager(this);
        teamSelector = new TeamSelector(this);

        arenaManager = new ArenaManager(this);
        arenaManager.setupArenas();

        kitSelector = new KitSelector(this);
        arenaVoter = new ArenaVoter(this);

        db = new Database();
        economyManager = new EconomyManager(this);

        kitManager = new KitManager(this);
        kitManager.setupKits();
        Bukkit.getPluginManager().registerEvents(kitManager, this);

        stateManager = new StateManager(this);
        stateManager.getState().start();

        Section dbConfig = resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled")) {
            db.init(dbConfig.getString("Uri"), dbConfig.getString("Database"), dbConfig.getString("Collection"));
        }

        if (resources.getScoreboard().getBoolean("Enabled")) {
            Assemble scoreboard = new Assemble(this, new GameScoreboard(this));
            scoreboard.setTicks(20);
            scoreboard.setAssembleStyle(AssembleStyle.MODERN);
        }

        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("reloadconfig").setExecutor(new ReloadConfigCommand(resources));
        getCommand("start").setExecutor(new StartCommand(this));
    }

    @Override
    public void onDisable() {
        kitManager.destroyNpcs();

        for (Player player : Bukkit.getOnlinePlayers()) {
            economyManager.uploadUser(player);
            kitManager.uploadUser(player);
        }
    }
}
