package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {

    public static boolean probability(double chance) {
        return chance > Math.random();
    }

    public static double randRange(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static double randSpread(double center, double spread) {
        return randRange(center - spread, center + spread);
    }

    public static double roundToHalf(double n) {
        return ((int) (2 * n + 0.5)) / 2.0;
    }

    public static boolean isBetween(double value, double min, double max) {
        return min <= value && value <= max;
    }

    public static double increasingLinear(double min, double max, double rangeAtMax, double x) {
        return x * (max - min) / rangeAtMax + min;
    }

    public static double decreasingLinear(double min, double max, double rangeAtMin, double x) {
        return -x * (max - min) / rangeAtMin + max;
    }

    public static Location ringPoint(Location center, float pitch, float yaw, double radius, double radians) {
        Vector displacement = new Vector();
        displacement.setX(Math.cos(radians) * radius);
        displacement.setZ(Math.sin(radians) * radius);
        VectorUtils.aroundX(displacement, (pitch + 90) * Math.PI / 180);
        VectorUtils.aroundY(displacement, -yaw * Math.PI / 180);
        return center.clone().add(displacement);
    }

    public static List<Location> getLocationCube(Location center, int size) {
        List<Location> locations = new ArrayList<>();

        int mid = size / 2;
        boolean isOdd = size % 2 == 0;
        double startX, startY, startZ;

        if (isOdd) {
            startX = center.getX() - size + 1;
            startY = center.getY() - size + 1;
            startZ = center.getZ() - size + 1;

        } else {
            locations.add(center.clone());

            startX = center.getX() - 0.5 * size + 0.5;
            startY = center.getY() - 0.5 * size + 0.5;
            startZ = center.getZ() - 0.5 * size + 0.5;
        }

        double x = startX;

        for (int i = 0; i < size; i++, x++) {
            double y = startY;

            for (int j = 0; j < size; j++, y++) {
                double z = startZ;

                for (int k = 0; k < size; k++, z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);

                    if (isOdd && i == mid && j == mid && k == mid) {
                        locations.add(0, loc);

                    } else {
                        locations.add(loc);
                    }
                }
            }
        }

        return locations;
    }
}
