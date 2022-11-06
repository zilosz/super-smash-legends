package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {

    public static Vector fromTo(Location from, Location to) {
        return to.toVector().subtract(from.toVector());
    }
}
