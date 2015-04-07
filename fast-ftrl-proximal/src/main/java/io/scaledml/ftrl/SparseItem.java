package io.scaledml.ftrl;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class SparseItem {
    private final LongList indexes = new LongArrayList();
    private final DoubleList values = new DoubleArrayList();
    private double label;

    public void addIndex(long index) {
        indexes.add(index);
        values.add(1.);
    }

    public void addIndex(long index, double value) {
        indexes.add(index);
        values.add(value);
    }

    public LongList indexes() {
        return indexes;
    }

    public DoubleList values() {
        return values;
    }

    public SparseItem label(double label) {
        this.label = label;
        return this;
    }

    public double label() {
        return label;
    }

    public void clear() {
        label = 0.;
        indexes.clear();
        values.clear();
    }
}
