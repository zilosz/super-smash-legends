package com.github.zilosz.ssl.util.collection;

import com.github.zilosz.ssl.util.math.MathUtils;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionUtils {

  public static <T> List<List<T>> rankElements(Iterable<T> elements, Comparator<T> comp) {
    return rankElements(elements, comp, Integer.MAX_VALUE);
  }

  public static <T> List<List<T>> rankElements(
      Iterable<T> elements, Comparator<T> comp, int maxElements
  ) {
    List<List<T>> ranked = new ArrayList<>();

    if (maxElements <= 0) {
      return ranked;
    }

    List<T> list = Lists.newArrayList(elements);
    list.sort(comp);

    List<T> currGroup = new ArrayList<>();
    int totalAdded = 0;

    for (int i = 0; i < list.size(); i++) {

      if (i > 0 && comp.compare(list.get(i - 1), list.get(i)) != 0) {
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

  public static <T> List<T> maxElementByInt(Iterable<T> items, Function<T, Integer> key) {
    return maxElement(items, key.andThen(Integer::doubleValue));
  }

  public static <T> List<T> maxElement(Iterable<T> items, Function<T, Double> key) {
    List<T> highestList = new ArrayList<>();
    double highestScore = Integer.MIN_VALUE;

    for (T item : items) {
      double score = key.apply(item);

      if (score == highestScore) {
        highestList.add(item);
      }
      else if (score > highestScore) {
        highestList = new ArrayList<>();
        highestList.add(item);
        highestScore = score;
      }
    }

    return highestList;
  }

  public static <T> T randChoice(List<T> items) {
    return items.get((int) MathUtils.randRange(0, items.size()));
  }

  public static <T> T randChoice(T[] items) {
    return items[(int) MathUtils.randRange(0, items.length)];
  }

  public static <K, V> void clearOverValues(Map<K, V> map, Consumer<V> onVal) {
    clearOverEntries(map, key -> {}, onVal);
  }

  public static <K, V> void clearOverEntries(
      Map<K, V> map, Consumer<K> onKey, Consumer<V> onVal
  ) {
    clearWhileIterating(map.entrySet(), entry -> {
      onKey.accept(entry.getKey());
      onVal.accept(entry.getValue());
    });
  }

  public static <T> void clearWhileIterating(Iterable<T> iterable, Consumer<T> onElement) {
    clearWhileIterating(iterable, onElement, el -> true);
  }

  public static <T> void clearWhileIterating(
      Iterable<T> elements, Consumer<T> onElement, Predicate<T> pred
  ) {
    Iterator<T> iterator = elements.iterator();

    while (iterator.hasNext()) {
      T next = iterator.next();

      if (pred.test(next)) {
        onElement.accept(next);
        iterator.remove();
      }
    }
  }

  public static <K, V> void clearOverValues(
      Map<K, V> map, BiConsumer<K, V> kv
  ) {
    clearWhileIterating(map.entrySet(), entry -> kv.accept(entry.getKey(), entry.getValue()));
  }
}
