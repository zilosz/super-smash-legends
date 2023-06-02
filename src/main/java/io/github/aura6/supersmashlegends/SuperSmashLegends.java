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
import io.github.aura6.supersmashlegends.utils.entity.NpcManager;
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

@Getter
public class SuperSmashLegends extends JavaPlugin {
    private Resources resources;
    private Database db;
    private KitManager kitManager;
    private InventoryManager inventoryManager;
    private KitSelector kitSelector;
    private ArenaVoter arenaVoter;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private TeamSelector teamSelector;
    private WorldManager worldManager;
    private DamageManager damageManager;
    private PowerManager powerManager;
    private NpcManager npcManager;

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
        npcManager = new NpcManager();

        Section dbConfig = resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled")) {
            db.init(dbConfig.getString("Uri"), dbConfig.getString("Database"), dbConfig.getString("Collection"));
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this.kitManager, this);
        pluginManager.registerEvents(this.npcManager, this);

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
        this.kitManager.destroyNpcs();

        try {
            this.gameManager.getState().end();
        } catch (IllegalPluginAccessException ignored) {}

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kitManager.wipePlayer(player);
        }
    }
}
