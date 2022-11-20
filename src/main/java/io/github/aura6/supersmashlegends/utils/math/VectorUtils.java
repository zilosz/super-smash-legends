package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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

    public static BukkitTask rotateAroundEntity(Plugin plugin, Entity rotating, Entity reference, float pitch, float yaw, double yOffset, double radius, int pointCount) {

        return new BukkitRunnable() {
            final double speed = 2 * radius * Math.PI / pointCount;
            final double radianStep = 2 * Math.PI / pointCount;
            double radians = 0;

            @Override
            public void run() {

                if (!rotating.isValid() || !reference.isValid()) {
                    rotating.remove();
                    cancel();
                    return;
                }

                Location referenceLoc = reference.getLocation().add(0, yOffset, 0);
                Location nextLoc = MathUtils.ringPoint(referenceLoc, pitch, yaw, radius, radians);
                Vector direction = fromTo(rotating.getLocation(), nextLoc).normalize();
                rotating.setVelocity(direction.multiply(speed).add(new Vector(0, 0.1, 0)));

                radians = radians >= 2 * Math.PI ? 0 : radians + radianStep;
            }

        }.runTaskTimer(plugin, 0, 0);
    }
}
