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
import com.github.zilosz.ssl.config.Resources;
import com.github.zilosz.ssl.database.PlayerDatabase;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.util.file.FileUtility;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import com.github.zilosz.ssl.util.world.WorldManager;
import fr.minuskube.inv.InventoryManager;
import io.github.thatkawaiisam.assemble.Assemble;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

@Getter
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
    FileUtility.deleteWorld(getLogger(), CustomWorldType.LOBBY.getWorldName());
    FileUtility.deleteWorld(getLogger(), CustomWorldType.ARENA.getWorldName());
  }

  @Override
  public void onDisable() {
    kitManager.destroyPodiums();

    for (Player player : Bukkit.getOnlinePlayers()) {
      kitManager.wipePlayer(player);
      playerDatabase.savePlayerData(player);
      playerDatabase.removePlayerData(player);
    }
  }

  @Override
  public void onEnable() {
    instance = this;

    resources = new Resources();
    damageManager = new AttackManager();
    teamManager = new TeamManager();
    arenaManager = new ArenaManager();
    npcRegistry = CitizensAPI.createNamedNPCRegistry("ssl-registry", new MemoryNPCDataStore());

    playerDatabase = new PlayerDatabase();
    playerDatabase.connect();

    worldManager = new WorldManager();
    Vector pasteVector = YamlReader.vector(resources.getLobby().getString("PasteVector"));
    File schematic = FileUtility.loadSchematic(this, "lobby");
    worldManager.createWorld(CustomWorldType.LOBBY, schematic, pasteVector);

    PluginManager pluginManager = Bukkit.getPluginManager();

    kitManager = new KitManager();
    kitManager.setupKits();
    pluginManager.registerEvents(kitManager, this);

    inventoryManager = new InventoryManager(this);
    inventoryManager.init();

    gameManager = new GameManager();
    gameManager.activateState();

    new Assemble(this, new GameScoreboard()).setTicks(5);

    getCommand("kit").setExecutor(new KitCommand());
    getCommand("reloadconfig").setExecutor(new ReloadConfigCommand());
    getCommand("start").setExecutor(new StartCommand());
    getCommand("end").setExecutor(new EndCommand());
    getCommand("skip").setExecutor(new SkipCommand());
    getCommand("loc").setExecutor(new LocCommand());
    getCommand("damage").setExecutor(new DamageCommand());
    getCommand("spec").setExecutor(new SpecCommand());
    getCommand("play").setExecutor(new PlayCommand());
    getCommand("heal").setExecutor(new HealCommand());

    DummyCommand dummyCommand = new DummyCommand();
    getCommand("dummy").setExecutor(dummyCommand);
    pluginManager.registerEvents(dummyCommand, this);
  }
}
