package io.scaledml;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SparseItem implements Serializable {
    private final LongList indexes = new LongArrayList();
    private double label;

    public void addIndex(long index) {
        indexes.add(index);
    }

    public LongList getIndexes() {
        return indexes;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public double getLabel() {
        return label;
    }
}
