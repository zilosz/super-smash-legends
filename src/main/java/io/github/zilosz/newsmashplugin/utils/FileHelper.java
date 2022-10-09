package io.github.zilosz.newsmashplugin.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class FileHelper {

    public static String buildPath(String... directories) {
        StringBuilder path = new StringBuilder(File.separator);

        for (String directory : directories) {
            path.append(directory).append(File.separator);
        }

        return path.toString();
    }

    public static YamlDocument loadYaml(Plugin plugin, String path) throws IOException {
        String fullPath = buildPath("config") + path + ".yml";
        return YamlDocument.create(new File(plugin.getDataFolder() + fullPath), plugin.getResource(fullPath));
    }

    public static File loadSchematic(Plugin plugin, String path) {
        String fullPath = buildPath("schematic") + path + ".schematic";
        File schematic = new File(plugin.getDataFolder() + fullPath);

        if (schematic.exists()) {
            return schematic;
        }

        plugin.saveResource(fullPath, false);
        return schematic;
    }
}
