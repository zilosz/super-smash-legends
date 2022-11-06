package io.github.aura6.supersmashlegends.utils.math;

import java.util.List;

public class MathUtils {

    public static int randRange(int min, int mix) {
        return (int) (Math.random() * (mix - min) + min);
    }

    public static <T> T selectRandom(List<T> items) {
        return items.get(randRange(0, items.size()));
    }
}
