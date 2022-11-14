package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class VectorUtils {

    public static Vector fromTo(Location from, Location to) {
        return to.toVector().subtract(from.toVector());
    }

    public static Vector fromTo(Entity from, Entity to) {
        return fromTo(from.getLocation(), to.getLocation());
    }

    public static Vector randVector(BlockFace face) {
        double minX = -1, maxX = 1, minY = -1, maxY = 1, minZ = -1, maxZ = 1;

        if (face != null) {

            switch (face) {

                case DOWN:
                    minY = 0;
                    break;

                case UP:
                    maxY = 0;
                    break;

                case WEST:
                    minX = 0;
                    break;

                case EAST:
                    maxX = 0;
                    break;

                case SOUTH:
                    minZ = 0;
                    break;

                default:
                    maxZ = 0;
            }
        }

        double x = MathUtils.randRange(minX, maxX);
        double y = MathUtils.randRange(minY, maxY);
        double z = MathUtils.randRange(minZ, maxZ);

        return new Vector(x, y, z).normalize();
    }

    public static void aroundY(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * vector.getX() + angleSin * vector.getZ();
        double z = -angleSin * vector.getX() + angleCos * vector.getZ();

        vector.setX(x).setZ(z);
    }

    public static void aroundX(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double y = angleCos * vector.getY() - angleSin * vector.getZ();
        double z = angleSin * vector.getY() + angleCos * vector.getZ();

        vector.setY(y).setZ(z);
    }
}
