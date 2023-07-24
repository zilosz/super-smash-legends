package com.github.zilosz.ssl;

import com.github.zilosz.ssl.arena.ArenaManager;
import com.github.zilosz.ssl.attack.AttackManager;
import com.github.zilosz.ssl.command.implementation.DamageCommand;
import com.github.zilosz.ssl.command.implementation.DummyCommand;
import com.github.zilosz.ssl.command.implementation.EndCommand;
import com.github.zilosz.ssl.command.implementation.HealCommand;
import com.github.zilosz.ssl.command.implementation.KitCommand;
import com.github.zilosz.ssl.command.implementation.LocCommand;
import com.github.zilosz.ssl.command.implementation.PlayCommand;
import com.github.zilosz.ssl.command.implementation.ReloadConfigCommand;
import com.github.zilosz.ssl.command.implementation.SkipCommand;
import com.github.zilosz.ssl.command.implementation.SpecCommand;
import com.github.zilosz.ssl.command.implementation.StartCommand;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.world.CustomWorldType;
import com.github.zilosz.ssl.utils.world.WorldManager;
import fr.minuskube.inv.InventoryManager;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    private NPCRegistry npcRegistry;

    @Override
    public void onLoad() {
        FileUtility.deleteWorld(CustomWorldType.LOBBY.getWorldName());
        FileUtility.deleteWorld(CustomWorldType.ARENA.getWorldName());
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kitManager.wipePlayer(player);
            this.playerDatabase.savePlayerData(player);
            this.playerDatabase.removePlayerData(player);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        this.resources = new Resources();
        this.damageManager = new AttackManager();
        this.teamManager = new TeamManager();
        this.arenaManager = new ArenaManager();
        this.npcRegistry = CitizensAPI.createNamedNPCRegistry("ssl-registry", new MemoryNPCDataStore());

        this.playerDatabase = new PlayerDatabase();
        this.playerDatabase.connect();

        this.worldManager = new WorldManager();
        Vector pasteVector = YamlReader.vector(this.resources.getLobby().getString("PasteVector"));
        File schematic = FileUtility.loadSchematic(this, "lobby");
        this.worldManager.createWorld(CustomWorldType.LOBBY, schematic, pasteVector);

        this.kitManager = new KitManager();
        this.kitManager.setupKits();

        this.inventoryManager = new InventoryManager(this);
        this.inventoryManager.init();

        this.gameManager = new GameManager();
        this.gameManager.activateState();

        new Assemble(this, new GameScoreboard()).setTicks(5);

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
}
