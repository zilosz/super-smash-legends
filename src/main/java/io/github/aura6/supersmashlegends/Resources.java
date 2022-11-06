package io.github.aura6.supersmashlegends;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.implementation.AxeThrow;
import io.github.aura6.supersmashlegends.attribute.implementation.GooeyBullet;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.HotbarItem;
import io.github.aura6.supersmashlegends.utils.Reflector;
import io.github.aura6.supersmashlegends.utils.file.FileLoader;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Resources {
    private final SuperSmashLegends plugin;

    private final YamlDocument kits;
    private final YamlDocument abilities;
    @Getter private final YamlDocument lobby;
    @Getter private final YamlDocument config;
    @Getter private final YamlDocument arenas;
    @Getter private final YamlDocument scoreboard;
    private final YamlDocument items;

    private final Map<String, Class<? extends Ability>> abilityRegistry = new HashMap<>();

    public Resources(SuperSmashLegends plugin) {
        this.plugin = plugin;

        kits = FileLoader.loadYaml(plugin, "kits");
        abilities = FileLoader.loadYaml(plugin, "abilities");

        lobby = FileLoader.loadYaml(plugin, "lobby");
        config = FileLoader.loadYaml(plugin, "config");
        arenas = FileLoader.loadYaml(plugin, "arenas");
        scoreboard = FileLoader.loadYaml(plugin, "scoreboard");
        items = FileLoader.loadYaml(plugin, "items");

        abilityRegistry.put("AxeThrow", AxeThrow.class);
        abilityRegistry.put("GooeyBullet", GooeyBullet.class);
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
        reloadDocument(abilities);
        reloadDocument(lobby);
        reloadDocument(config);
        reloadDocument(arenas);
    }

    public Ability loadAbility(String name, Kit kit, int slot) {
        Section section = abilities.getSection(kit.getConfigName() + "." + name);
        return Reflector.newInstance(abilityRegistry.get(name), plugin, section, kit, slot);
    }

    public Kit loadKit(String name) {
        return new Kit(plugin, kits.getSection(name));
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

    public void loadAndRegisterHotbarItem(String path, Player player, int slot, Consumer<PlayerInteractEvent> action) {
        ItemStack stack = YamlReader.readItemStack(items.getSection(path));
        HotbarItem hotbarItem = new HotbarItem(player, stack, slot);
        hotbarItem.setAction(action);
        hotbarItem.register(plugin);
    }
}
