package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class VectorUtils {

    public static Set<Location> getRectLocations(Location center, Vector alternative, double width, double height, int count) {
        Vector direction = center.getDirection();

        if (direction.getX() == 0 && direction.getZ() == 0) {
            direction = alternative.clone().normalize();
        }

        Vector unitHorizontal = direction.getCrossProduct(new Vector(0, 1, 0)).normalize();
        Vector unitVertical = direction.getCrossProduct(unitHorizontal).normalize();

        Set<Location> locations = new HashSet<>();

        for (int i = 0; i < count; i++) {
            double w = MathUtils.randRange(-width / 2, width / 2);
            double h = MathUtils.randRange(-height / 2, height / 2);

            Vector horizontal = unitHorizontal.clone().multiply(w);
            Vector vertical = unitVertical.clone().multiply(h);

            locations.add(center.clone().add(horizontal).add(vertical));
        }

        return locations;
    }

    public static Set<Vector> getConicVectors(Location source, double angle, int count) {
        Set<Vector> vectors = new HashSet<>();

        double radians = 0;
        double angleStep = MathUtils.degToRad(360.0 / count);
        double ringRadius = Math.tan(MathUtils.degToRad(angle));

        Location center = source.clone().add(source.getDirection());

        for (int i = 0; i < count; i++) {
            Location ringPoint = MathUtils.ringPoint(center, ringRadius, radians);
            vectors.add(fromTo(source, ringPoint).normalize());
            radians += angleStep;
        }

        return vectors;
    }

    public static Vector getRandomVectorInDirection(Location source, double maxAngle) {
        double angle = MathUtils.randRange(0, maxAngle);
        double radians = MathUtils.randRange(0, 2 * Math.PI);
        double length = Math.tan(MathUtils.degToRad(angle));

        Location ringCenter = source.clone().add(source.getDirection());
        Location ringPoint = MathUtils.ringPoint(ringCenter, length, radians);

        return fromTo(source, ringPoint).normalize();
    }

    public static Vector getHorizontallyTiltedVector(Location source, Vector alternative, double angle) {
        Vector direction = source.getDirection();

        if (direction.getX() == 0 && direction.getZ() == 0) {
            direction = alternative.clone().normalize();
        }

        Vector unitHorizontal = direction.getCrossProduct(new Vector(0, 1, 0)).normalize();
        double amountHorizontal = Math.tan(MathUtils.degToRad(angle));
        Location target = source.clone().add(unitHorizontal.multiply(amountHorizontal)).add(direction);

        return fromTo(source, target).normalize();
    }

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

                case UP:
                    minY = 0;
                    break;

                case DOWN:
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

                case NORTH:
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

                radians += radianStep;

                if (radians >= 2 * Math.PI) {
                    radians = 0;
                }
            }

        }.runTaskTimer(plugin, 0, 0);
    }
}
