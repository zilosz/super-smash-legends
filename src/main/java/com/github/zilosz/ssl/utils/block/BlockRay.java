package com.github.zilosz.ssl.utils.block;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class BlockRay {
    private final Location location;
    private final Vector direction;
    private Location emptyDestination;
    @Getter private Block hitBlock;

    public BlockRay(Location location, Vector direction) {
        this.location = location.clone();
        this.direction = direction.clone();
    }

    public BlockRay(Location location) {
        this(location, location.getDirection());
    }

    public void cast(int range) {
        BlockIterator iterator = new BlockIterator(location.getWorld(), location.toVector(), direction, 0, range);
        emptyDestination = location.clone();

        while (iterator.hasNext()) {
            hitBlock = iterator.next();

            if (!hitBlock.isEmpty()) {
                break;
            }

            emptyDestination = hitBlock.getLocation();
        }

        emptyDestination.setDirection(direction);
    }

    public Location getLocation() {
        return location.clone();
    }

    public Location getEmptyDestination() {
        return emptyDestination.clone();
    }
}
