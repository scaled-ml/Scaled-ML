package io.scaledml.core;

import com.google.common.base.MoreObjects;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.core.util.Util;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.BitSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    public SparseItem addIndex(long ind, double value) {
        long index = Math.abs(ind) % (1L << 40);
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

    public void write(LineBytesBuffer line) {
        line.putByte((byte) Math.signum(label));
        line.putString(id);
        line.putShort((short) indexes.size());
        for (long index : indexes) {
            line.putLong(index);
        }
    }

    public void read(LineBytesBuffer line) {
        clear();
        AtomicInteger cursor = new AtomicInteger(0);
        label = line.readByte(cursor) > 0 ? 1. : 0;
        id = line.readString(cursor);
        short size = line.readShort(cursor);
        for (short i = 0; i < size; i++) {
            indexes.add(line.readLong(cursor));
            values.add(1.);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SparseItem that = (SparseItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(indexes, that.indexes) &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, indexes, values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id)
                .add("label", label)
                .add("indexes", indexes)
                .add("values", values).toString();
    }
}
