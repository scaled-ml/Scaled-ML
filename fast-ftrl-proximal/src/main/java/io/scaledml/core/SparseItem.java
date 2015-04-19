package io.scaledml.core;

import io.scaledml.ftrl.util.Util;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class SparseItem {
    private final LongList indexes = new LongArrayList();
    private final DoubleList values = new DoubleArrayList();
    private String id;
    private double label;

    public SparseItem addIndex(long index) {
        indexes.add(index);
        values.add(1.);
        return this;
    }

    public SparseItem addIndex(long index, double value) {
        indexes.add(index);
        values.add(value);
        return this;
    }

    public LongList indexes() {
        return indexes;
    }

    public DoubleList values() {
        return values;
    }

    public SparseItem label(double label) {
        this.label = Util.doublesEqual(1., label) ? 1. : 0.;
        return this;
    }

    public SparseItem id(String id) {
        this.id = id;
        return this;
    }

    public double label() {
        return label;
    }

    public void clear() {
        label = 0.;
        id = null;
        indexes.clear();
        values.clear();
    }

    public String id() {
        return id;
    }
}
