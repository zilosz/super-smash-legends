package com.github.zilosz.ssl.util.block;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class BlockRay {
  private final Location loc;
  private final Vector dir;
  private Location emptyLoc;
  @Getter private Block hitBlock;

  public BlockRay(Location location) {
    this(location, location.getDirection());
  }

  public BlockRay(Location loc, Vector dir) {
    this.loc = loc.clone();
    this.dir = dir.clone();
  }

  public void cast(int range) {
    BlockIterator it = new BlockIterator(loc.getWorld(), loc.toVector(), dir, 0, range);
    emptyLoc = loc.clone();

    while (it.hasNext()) {
      hitBlock = it.next();

      if (!hitBlock.isEmpty()) {
        break;
      }

      emptyLoc = hitBlock.getLocation();
    }

    emptyLoc.setDirection(dir);
  }

  public Location getEmptyLoc() {
    return emptyLoc.clone();
  }
}
