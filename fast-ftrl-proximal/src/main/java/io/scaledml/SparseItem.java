package io.scaledml;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SparseItem {
    private final LongList indexes = new LongArrayList();
    private double label;

    public void addIndex(long index) {
        indexes.add(index);
    }

    public LongList indexes() {
        return indexes;
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
    }
}
