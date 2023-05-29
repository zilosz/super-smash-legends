package io.github.aura6.supersmashlegends;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import fr.minuskube.inv.InventoryManager;
import io.github.aura6.supersmashlegends.arena.ArenaManager;
import io.github.aura6.supersmashlegends.command.DummyCommand;
import io.github.aura6.supersmashlegends.command.EndCommand;
import io.github.aura6.supersmashlegends.command.KitCommand;
import io.github.aura6.supersmashlegends.command.LocCommand;
import io.github.aura6.supersmashlegends.command.ReloadConfigCommand;
import io.github.aura6.supersmashlegends.command.SkipCommand;
import io.github.aura6.supersmashlegends.command.StartCommand;
import io.github.aura6.supersmashlegends.damage.DamageManager;
import io.github.aura6.supersmashlegends.database.Database;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.game.GameScoreboard;
import io.github.aura6.supersmashlegends.arena.ArenaVoter;
import io.github.aura6.supersmashlegends.kit.KitSelector;
import io.github.aura6.supersmashlegends.team.TeamSelector;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.power.PowerManager;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.WorldManager;
import io.github.aura6.supersmashlegends.utils.file.FileUtility;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

public class SuperSmashLegends extends JavaPlugin {
    @Getter private Resources resources;
    @Getter private Database db;
    @Getter private KitManager kitManager;
    @Getter private InventoryManager inventoryManager;
    @Getter private KitSelector kitSelector;
    @Getter private ArenaVoter arenaVoter;
    @Getter private GameManager gameManager;
    @Getter private ArenaManager arenaManager;
    @Getter private TeamManager teamManager;
    @Getter private TeamSelector teamSelector;
    @Getter private WorldManager worldManager;
    @Getter private DamageManager damageManager;
    @Getter private PowerManager powerManager;

    @Override
    public void onLoad() {
        FileUtility.deleteWorld("lobby");
        FileUtility.deleteWorld("arena");
    }

    @Override
    public void onEnable() {
        resources = new Resources(this);
        damageManager = new DamageManager(this);
        worldManager = new WorldManager();
        inventoryManager = new InventoryManager(this);
        teamManager = new TeamManager(this);
        teamSelector = new TeamSelector(this);
        arenaManager = new ArenaManager(this);
        kitSelector = new KitSelector(this);
        arenaVoter = new ArenaVoter(this);
        db = new Database();
        kitManager = new KitManager(this);
        gameManager = new GameManager(this);
        powerManager = new PowerManager(this);

        Section dbConfig = resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled")) {
            db.init(dbConfig.getString("Uri"), dbConfig.getString("Database"), dbConfig.getString("Collection"));
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(kitManager, this);
        pluginManager.registerEvents(powerManager, this);

        Vector pasteVector = YamlReader.vector(resources.getLobby().getString("PasteVector"));
        File schematic = FileUtility.loadSchematic(this, "lobby");
        worldManager.createWorld("lobby", schematic, pasteVector);

        inventoryManager.init();
        kitManager.setupKits();
        gameManager.activateState();

        Assemble scoreboard = new Assemble(this, new GameScoreboard(this));
        scoreboard.setTicks(5);

        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("reloadconfig").setExecutor(new ReloadConfigCommand(resources));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("end").setExecutor(new EndCommand(this));
        getCommand("skip").setExecutor(new SkipCommand(this));
        getCommand("dummy").setExecutor(new DummyCommand());
        getCommand("loc").setExecutor(new LocCommand());
    }

    @Override
    public void onDisable() {
        kitManager.destroyNpcs();

        try {
            gameManager.getState().end();
        } catch (IllegalPluginAccessException ignored) {}

        for (Player player : Bukkit.getOnlinePlayers()) {
            kitManager.wipePlayer(player);
            kitManager.getSelectedKit(player).deactivate();
        }
    }
}
