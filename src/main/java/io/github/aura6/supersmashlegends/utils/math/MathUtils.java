package io.github.aura6.supersmashlegends.utils.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MathUtils {

    public static double randRange(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static double randSpread(double center, double spread) {
        return randRange(center - spread, center + spread);
    }

    public static double increasingLinear(double min, double max, double rangeAtMax, double x) {
        return x * (max - min) / rangeAtMax + min;
    }

    public static double decreasingLinear(double min, double max, double rangeAtMin, double x) {
        return -x * (max - min) / rangeAtMin + max;
    }

    public static <T> T selectRandom(List<T> items) {
        return items.get((int) randRange(0, items.size()));
    }

    public static <T> List<T> findByHighestDouble(List<T> items, Function<T, Double> key) {
        List<T> highestList = new ArrayList<>();
        double highestScore = Integer.MIN_VALUE;

        for (T item : items) {
            double score = key.apply(item);

            if (score == highestScore) {
                highestList.add(item);

            } else if (score > highestScore) {
                highestList = new ArrayList<>();
                highestList.add(item);
                highestScore = score;
            }
        }

        return highestList;
    }

    public static <T> List<T> findByHighestInt(List<T> items, Function<T, Integer> key) {
        return findByHighestDouble(items, key.andThen(Integer::doubleValue));
    }

    public static Location ringPoint(Location center, float pitch, float yaw, double radius, double radians) {
        Vector displacement = new Vector();
        displacement.setX(Math.cos(radians) * radius);
        displacement.setZ(Math.sin(radians) * radius);
        VectorUtils.aroundX(displacement, (pitch + 90) * Math.PI / 180);
        VectorUtils.aroundY(displacement, -yaw * Math.PI / 180);
        return center.clone().add(displacement);
    }
}
