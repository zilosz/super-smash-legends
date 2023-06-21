package com.github.zilosz.ssl.utils;

import com.google.common.collect.Lists;
import com.github.zilosz.ssl.utils.math.MathUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

    public static <T> List<T> findByHighestDouble(Iterable<T> items, Function<T, Double> key) {
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

    public static <T> List<T> findByHighestInt(Iterable<T> items, Function<T, Integer> key) {
        return findByHighestDouble(items, key.andThen(Integer::doubleValue));
    }

    public static <T> T selectRandom(List<T> items) {
        return items.get((int) MathUtils.randRange(0, items.size()));
    }

    public static <T> T selectRandom(T[] items) {
        return items[(int) MathUtils.randRange(0, items.length)];
    }

    public static <T> void removeWhileIterating(Iterable<T> iterable, Consumer<T> action) {
        Iterator<T> iterator = iterable.iterator();

        while (iterator.hasNext()) {
            action.accept(iterator.next());
            iterator.remove();
        }
    }

    public static <K, V> void removeWhileIteratingFromMap(Map<K, V> map, Consumer<K> keyAction, Consumer<V> valueAction) {
        removeWhileIterating(map.entrySet(), entry -> {
            keyAction.accept(entry.getKey());
            valueAction.accept(entry.getValue());
        });
    }

    public static <K, V> void removeWhileIteratingFromMap(Map<K, V> map, Consumer<V> valueAction) {
        removeWhileIteratingFromMap(map, key -> {}, valueAction);
    }
}
