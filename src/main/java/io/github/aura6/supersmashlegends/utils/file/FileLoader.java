package io.github.aura6.supersmashlegends.utils.file;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class FileLoader {

    public static YamlDocument loadYaml(Plugin plugin, String path) {
        String fullPath = path + ".yml";
        try {
            return YamlDocument.create(new File(plugin.getDataFolder(), fullPath), plugin.getResource(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File loadSchematic(Plugin plugin, String path) {
        String fullPath = PathBuilder.build("schematic", path) + ".schematic";
        plugin.saveResource(fullPath, false);
        return new File(plugin.getDataFolder(), fullPath);
    }
}
