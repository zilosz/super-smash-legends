package io.github.aura6.supersmashlegends.utils;

import com.google.common.collect.Lists;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class CollectionUtils {

    public static <T> List<List<T>> getRankedGroups(Iterable<T> elements, Comparator<T> comparator, int maxElements) {
        List<List<T>> ranked = new ArrayList<>();

        if (maxElements <= 0) return ranked;

        List<T> list = Lists.newArrayList(elements);
        list.sort(comparator);

        List<T> currGroup = new ArrayList<>();
        int totalAdded = 0;

        for (int i = 0; i < list.size(); i++) {

            if (i > 0 && comparator.compare(list.get(i - 1), list.get(i)) != 0) {
                ranked.add(new ArrayList<>(currGroup));
                currGroup.clear();
            }

            currGroup.add(list.get(i));

            if (++totalAdded == maxElements) {
                break;
            }
        }

        ranked.add(currGroup);
        return ranked;
    }

    public static <T> List<List<T>> getRankedGroups(Iterable<T> elements, Comparator<T> comparator) {
        return getRankedGroups(elements, comparator, Integer.MAX_VALUE);
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

    public static <T> T selectRandom(List<T> items) {
        return items.get((int) MathUtils.randRange(0, items.size()));
    }
}
