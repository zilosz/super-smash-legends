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
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.WorldManager;
import io.github.aura6.supersmashlegends.utils.file.FileUtility;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

@Getter
public class SuperSmashLegends extends JavaPlugin {
    @Getter private static SuperSmashLegends instance;

    private Resources resources;
    private Database db;
    private KitManager kitManager;
    private InventoryManager inventoryManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private WorldManager worldManager;
    private DamageManager damageManager;

    @Override
    public void onLoad() {
        FileUtility.deleteWorld("lobby");
        FileUtility.deleteWorld("arena");
    }

    @Override
    public void onEnable() {
        instance = this;

        this.resources = new Resources(this);
        this.damageManager = new DamageManager(this);
        this.worldManager = new WorldManager();
        this.inventoryManager = new InventoryManager(this);
        this.teamManager = new TeamManager(this);
        this.arenaManager = new ArenaManager(this);
        this.db = new Database();
        this.kitManager = new KitManager(this);
        this.gameManager = new GameManager(this);

        Section dbConfig = this.resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled")) {
            this.db.init(dbConfig.getString("Uri"), dbConfig.getString("Database"), dbConfig.getString("Collection"));
        }

        Bukkit.getPluginManager().registerEvents(this.kitManager, this);

        Vector pasteVector = YamlReader.vector(this.resources.getLobby().getString("PasteVector"));
        File schematic = FileUtility.loadSchematic(this, "lobby");
        this.worldManager.createWorld("lobby", schematic, pasteVector);

        this.inventoryManager.init();
        this.kitManager.setupKits();
        this.gameManager.activateState();

        Assemble scoreboard = new Assemble(this, new GameScoreboard(this));
        scoreboard.setTicks(5);

        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("reloadconfig").setExecutor(new ReloadConfigCommand(this.resources));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("end").setExecutor(new EndCommand(this));
        getCommand("skip").setExecutor(new SkipCommand(this));
        getCommand("dummy").setExecutor(new DummyCommand());
        getCommand("loc").setExecutor(new LocCommand());
    }

    @Override
    public void onDisable() {
        this.kitManager.destroyNpcs();

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kitManager.wipePlayer(player);
        }

        try {
            this.gameManager.getState().end();
        } catch (IllegalPluginAccessException ignored) {}
    }
}
