package com.github.zilosz.ssl;

import com.github.zilosz.ssl.arena.Arena;
import com.github.zilosz.ssl.utils.file.FileUtility;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.HotbarItem;
import com.github.zilosz.ssl.utils.file.YamlReader;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Resources {
    private final SSL plugin;

    private final YamlDocument kits;
    private final YamlDocument items;
    @Getter private final YamlDocument lobby;
    @Getter private final YamlDocument config;
    @Getter private final YamlDocument arenas;

    public Resources(SSL plugin) {
        this.plugin = plugin;

        kits = FileUtility.loadYaml(plugin, "kits");
        items = FileUtility.loadYaml(plugin, "items");
        lobby = FileUtility.loadYaml(plugin, "lobby");
        config = FileUtility.loadYaml(plugin, "config");
        arenas = FileUtility.loadYaml(plugin, "arenas");
    }

    public void reloadDocument(YamlDocument document) {
        try {
            document.reload();
            document.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        reloadDocument(kits);
        reloadDocument(lobby);
        reloadDocument(config);
        reloadDocument(arenas);
        reloadDocument(items);
    }

    public Kit loadKit(String name) {
        return new Kit(this.kits.getSection(name));
    }

    public List<Kit> loadKits() {
        return kits.getKeys().stream().map(key -> loadKit((String) key)).collect(Collectors.toList());
    }

    public Arena loadArena(String name) {
        return new Arena(plugin, arenas.getSection(name));
    }

    public List<Arena> loadArenas() {
        return arenas.getKeys().stream().map(key -> loadArena((String) key)).collect(Collectors.toList());
    }

    public HotbarItem giveHotbarItem(String path, Player player, Consumer<PlayerInteractEvent> action) {
        Section config = this.items.getSection(path);
        ItemStack stack = YamlReader.stack(config);
        HotbarItem hotbarItem = new HotbarItem(player, stack, config.getInt("Slot"));
        hotbarItem.setAction(action);
        hotbarItem.register(this.plugin);
        return hotbarItem;
    }
}
