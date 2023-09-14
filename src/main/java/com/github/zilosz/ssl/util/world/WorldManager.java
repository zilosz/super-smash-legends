package com.github.zilosz.ssl.util.world;

import com.boydti.fawe.object.schematic.Schematic;
import com.github.zilosz.ssl.SSL;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class WorldManager {
    private final Map<CustomWorldType, WorldInfo> worlds = new EnumMap<>(CustomWorldType.class);

    public void createWorld(CustomWorldType type, File schematic, Vector pastePoint) {

        WorldCreator worldCreator = WorldCreator.name(type.getWorldName())
                .type(WorldType.FLAT).generateStructures(false).generatorSettings("3;minecraft:air;2");

        com.sk89q.worldedit.Vector worldEditPastePoint = new com.sk89q.worldedit.Vector(
                pastePoint.getX(), pastePoint.getY(), pastePoint.getZ());

        Schematic schem;

        try {
            schem = ClipboardFormat.SCHEMATIC.load(schematic);

        } catch (IOException e) {
            String message = "Could not find the following schematic: '%s'";
            throw new RuntimeException(String.format(message, schematic.getName()));
        }

        World world = Bukkit.createWorld(worldCreator);
        EditSession editSession = schem.paste(new BukkitWorld(world), worldEditPastePoint);

        Section timeConfig = SSL.getInstance().getResources().getConfig().getSection("WorldTimeUpdater");

        BukkitTask timeUpdater = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            world.setTime(timeConfig.getInt("Time"));
        }, 0, timeConfig.getInt("Frequency"));

        this.worlds.put(type, new WorldInfo(editSession, timeUpdater));
    }

    public void resetWorld(CustomWorldType worldType) {
        Optional.ofNullable(this.worlds.remove(worldType)).ifPresent(worldInfo -> {
            worldInfo.editSession.undo(worldInfo.editSession);
            worldInfo.timeUpdater.cancel();
        });
    }

    private static class WorldInfo {
        private final EditSession editSession;
        private final BukkitTask timeUpdater;

        public WorldInfo(EditSession editSession, BukkitTask timeUpdater) {
            this.editSession = editSession;
            this.timeUpdater = timeUpdater;
        }
    }
}
