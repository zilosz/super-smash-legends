package com.github.zilosz.ssl.utils.collection;

import java.util.NavigableMap;
import java.util.TreeMap;

public class RandomCollection<E> {
    private final NavigableMap<Double, E> weights = new TreeMap<>();
    private double totalWeight = 0;

    public void add(E item, double weight) {
        this.totalWeight += weight;
        this.weights.put(this.totalWeight, item);
    }

    public E next() {
        return this.weights.higherEntry(this.totalWeight * Math.random()).getValue();
    }
}
