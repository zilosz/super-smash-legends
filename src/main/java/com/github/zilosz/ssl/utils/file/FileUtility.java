package com.github.zilosz.ssl.utils.file;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class FileUtility {

    public static YamlDocument loadYaml(Plugin plugin, String path) {
        String fullPath = path + ".yml";
        try {
            return YamlDocument.create(new File(plugin.getDataFolder(), fullPath), plugin.getResource(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadYaml(YamlDocument document) {
        try {
            document.reload();
            document.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File loadSchematic(Plugin plugin, String path) {
        String fullPath = buildPath("schematics", path) + ".schematic";
        plugin.saveResource(fullPath, false);
        return new File(plugin.getDataFolder(), fullPath);
    }

    public static String buildPath(String... parts) {
        StringBuilder path = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            path.append(parts[i]);

            if (i < parts.length - 1) {
                path.append(File.separator);
            }
        }

        return path.toString();
    }

    public static void deleteWorld(String name) {
        File world = new File(Bukkit.getWorldContainer(), name);

        if (world.exists()) {

            try {
                FileUtils.deleteDirectory(world);
                Bukkit.getLogger().log(Level.INFO, String.format("Deleted the %s world.", name));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
