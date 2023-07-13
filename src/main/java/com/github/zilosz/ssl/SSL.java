package com.github.zilosz.ssl;

import com.github.zilosz.ssl.arena.ArenaManager;
import com.github.zilosz.ssl.attack.AttackManager;
import com.github.zilosz.ssl.command.DamageCommand;
import com.github.zilosz.ssl.command.DummyCommand;
import com.github.zilosz.ssl.command.EndCommand;
import com.github.zilosz.ssl.command.HealCommand;
import com.github.zilosz.ssl.command.KitCommand;
import com.github.zilosz.ssl.command.LocCommand;
import com.github.zilosz.ssl.command.PlayCommand;
import com.github.zilosz.ssl.command.ReloadConfigCommand;
import com.github.zilosz.ssl.command.SkipCommand;
import com.github.zilosz.ssl.command.SpecCommand;
import com.github.zilosz.ssl.command.StartCommand;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.utils.NpcStorage;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.world.StaticWorldType;
import com.github.zilosz.ssl.utils.world.WorldManager;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import fr.minuskube.inv.InventoryManager;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

@Getter
@SuppressWarnings("OverlyCoupledClass")
public class SSL extends JavaPlugin {
    @Getter private static SSL instance;

    private Resources resources;
    private PlayerDatabase playerDatabase;
    private KitManager kitManager;
    private InventoryManager inventoryManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private WorldManager worldManager;
    private AttackManager damageManager;
    private NpcStorage npcStorage;

    @Override
    public void onLoad() {
        FileUtility.deleteWorld(StaticWorldType.LOBBY.getWorldName());
        FileUtility.deleteWorld(StaticWorldType.ARENA.getWorldName());
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kitManager.wipePlayer(player);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        this.resources = new Resources();
        this.damageManager = new AttackManager();
        this.worldManager = new WorldManager();
        this.inventoryManager = new InventoryManager(this);
        this.teamManager = new TeamManager();
        this.arenaManager = new ArenaManager();
        this.playerDatabase = new PlayerDatabase();
        this.kitManager = new KitManager();
        this.gameManager = new GameManager();
        this.npcStorage = new NpcStorage();

        Section dbConfig = this.resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled") && Bukkit.getServer().getOnlineMode()) {
            String uri = dbConfig.getString("Uri");
            String db = dbConfig.getString("Database");
            String collection = dbConfig.getString("Collection");
            this.playerDatabase.init(uri, db, collection);
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this.kitManager, this);

        Vector pasteVector = YamlReader.vector(this.resources.getLobby().getString("PasteVector"));
        File schematic = FileUtility.loadSchematic(this, "lobby");
        this.worldManager.createWorld(StaticWorldType.LOBBY, schematic, pasteVector);

        this.kitManager.setupKits();
        this.inventoryManager.init();
        this.gameManager.activateState();

        Assemble scoreboard = new Assemble(this, new GameScoreboard());
        scoreboard.setTicks(5);

        this.getCommand("kit").setExecutor(new KitCommand());
        this.getCommand("reloadconfig").setExecutor(new ReloadConfigCommand());
        this.getCommand("start").setExecutor(new StartCommand());
        this.getCommand("end").setExecutor(new EndCommand());
        this.getCommand("skip").setExecutor(new SkipCommand());
        this.getCommand("loc").setExecutor(new LocCommand());
        this.getCommand("damage").setExecutor(new DamageCommand());
        this.getCommand("spec").setExecutor(new SpecCommand());
        this.getCommand("play").setExecutor(new PlayCommand());
        this.getCommand("heal").setExecutor(new HealCommand());

        DummyCommand dummyCommand = new DummyCommand();
        this.getCommand("dummy").setExecutor(dummyCommand);
        pluginManager.registerEvents(dummyCommand, this);
    }
}
