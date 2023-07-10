package com.github.zilosz.ssl;

import com.github.zilosz.ssl.arena.ArenaManager;
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
import com.github.zilosz.ssl.damage.DamageManager;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.utils.NpcStorage;
import com.github.zilosz.ssl.utils.WorldManager;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import fr.minuskube.inv.InventoryManager;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import net.citizensnpcs.api.CitizensPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

@Getter
@SuppressWarnings("OverlyCoupledClass")
public class SSL extends JavaPlugin implements Listener {
    @Getter private static SSL instance;

    private Resources resources;
    private PlayerDatabase playerDatabase;
    private KitManager kitManager;
    private InventoryManager inventoryManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private WorldManager worldManager;
    private DamageManager damageManager;
    private NpcStorage npcStorage;

    @Override
    public void onLoad() {
        FileUtility.deleteWorld("lobby");
        FileUtility.deleteWorld("arena");
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
        this.damageManager = new DamageManager();
        this.worldManager = new WorldManager();
        this.inventoryManager = new InventoryManager(this);
        this.teamManager = new TeamManager();
        this.arenaManager = new ArenaManager();
        this.playerDatabase = new PlayerDatabase();
        this.kitManager = new KitManager();
        this.gameManager = new GameManager();
        this.npcStorage = new NpcStorage();

        Section dbConfig = this.resources.getConfig().getSection("Database");

        if (dbConfig.getBoolean("Enabled")) {
            String uri = dbConfig.getString("Uri");
            String db = dbConfig.getString("Database");
            String collection = dbConfig.getString("Collection");
            this.playerDatabase.init(uri, db, collection);
        }

        Bukkit.getPluginManager().registerEvents(this.kitManager, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        Vector pasteVector = YamlReader.getVector(this.resources.getLobby().getString("PasteVector"));
        File schematic = FileUtility.loadSchematic(this, "lobby");
        this.worldManager.createWorld("lobby", schematic, pasteVector);

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
        Bukkit.getPluginManager().registerEvents(dummyCommand, this);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin() instanceof CitizensPlugin) {
            this.kitManager.setupKits();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() instanceof CitizensPlugin) {
            this.npcStorage.destroyNpcs();
        }
    }
}
