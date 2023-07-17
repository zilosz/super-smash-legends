package com.github.zilosz.ssl.utils.block;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class BlockRay {
    private final Location location;
    private final Vector direction;
    private Location emptyDestination;
    @Getter private Block hitBlock;

    public BlockRay(Location location) {
        this(location, location.getDirection());
    }

    public BlockRay(Location location, Vector direction) {
        this.location = location.clone();
        this.direction = direction.clone();
    }

    public void cast(int range) {
        World world = this.location.getWorld();
        BlockIterator iterator = new BlockIterator(world, this.location.toVector(), this.direction, 0, range);
        this.emptyDestination = this.location.clone();

        while (iterator.hasNext()) {
            this.hitBlock = iterator.next();

            if (!this.hitBlock.isEmpty()) {
                break;
            }

            this.emptyDestination = this.hitBlock.getLocation();
        }

        this.emptyDestination.setDirection(this.direction);
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public Location getEmptyDestination() {
        return this.emptyDestination.clone();
    }
}
