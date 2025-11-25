package com.github.zilosz.ssl.util.collection;

import java.util.NavigableMap;
import java.util.TreeMap;

public class RandomCollection<E> {
  private final NavigableMap<Double, E> weights = new TreeMap<>();
  private double totalWeight;

  public void add(E item, double weight) {
    totalWeight += weight;
    weights.put(totalWeight, item);
  }

  public E next() {
    return weights.higherEntry(totalWeight * Math.random()).getValue();
  }
}
