package io.scaledml.core;

import com.google.common.base.MoreObjects;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.core.util.MultiListsViewLongList;
import io.scaledml.core.util.Util;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SparseItem {
    private final LongList categoricalIndexes = new LongArrayList();
    private final LongList numericalIndexes = new LongArrayList();
    private final DoubleList numericalValues = new DoubleArrayList();
    private String id;
    private double label;

    private final LongList indexesView = new MultiListsViewLongList(categoricalIndexes, numericalIndexes);

    public SparseItem addCategoricalIndex(long index) {
        categoricalIndexes.add(normalizeIndex(index));
        return this;
    }

    public SparseItem addNumericalIndex(long index, double value) {
        numericalIndexes.add(normalizeIndex(index));
        numericalValues.add(value);
        return this;
    }

    private long normalizeIndex(long ind) {
        return Math.abs(ind) % (1L << 40);
    }

    public LongList indexes() {
        return indexesView;
    }

    public double getValue(int i) {
        return i < categoricalIndexes.size() ? 1 : numericalValues.getDouble(i - categoricalIndexes.size());
    }

    public SparseItem label(double label) {
        this.label = Util.doublesEqual(1., label) ? 1. : 0.;
        return this;
    }

    public LongList categoricalIndexes() {
        return categoricalIndexes;
    }

    public LongList numericalIndexes() {
        return numericalIndexes;
    }

    public DoubleList numericalValues() {
        return numericalValues;
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
        numericalIndexes.clear();
        numericalValues.clear();
        categoricalIndexes.clear();
    }

    public String id() {
        return id;
    }

    public void write(LineBytesBuffer line) {
        line.putByte((byte) Math.signum(label));
        line.putString(id);
        line.putShort((short) categoricalIndexes.size());
        for (long index : categoricalIndexes) {
            line.putLong(index);
        }
        line.putShort((short) numericalIndexes.size());
        for (int i = 0; i < numericalIndexes.size(); i++)  {
            line.putLong(numericalIndexes.getLong(i));
            line.putFloat((float) numericalValues.getDouble(i));
        }
    }

    public void read(LineBytesBuffer line) {
        clear();
        AtomicInteger cursor = new AtomicInteger(0);
        label = line.readByte(cursor) > 0 ? 1. : 0;
        id = line.readString(cursor);
        short factorsSize = line.readShort(cursor);
        for (short i = 0; i < factorsSize; i++) {
            categoricalIndexes.add(line.readLong(cursor));
        }
        short numericalSize = line.readShort(cursor);
        for (short i = 0; i < numericalSize; i++) {
            numericalIndexes.add(line.readLong(cursor));
            numericalValues.add(line.readFloat(cursor));
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
                Objects.equals(categoricalIndexes, that.categoricalIndexes) &&
                Objects.equals(numericalIndexes, that.numericalIndexes) &&
                equalNumericalValues(that);
    }

    private boolean equalNumericalValues(SparseItem that) {
        if (numericalValues.size() != that.numericalValues.size()) {
            return false;
        }
        for (int i = 0; i < numericalValues.size(); i++) {
            if (!Util.doublesEqual(numericalValues.getDouble(i), that.numericalValues.getDouble(i), 0.00001)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, categoricalIndexes, numericalIndexes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id)
                .add("label", label)
                .add("categorialIndexes", categoricalIndexes)
                .add("numericalIndexes", numericalIndexes)
                .add("numericalValues", numericalValues).toString();
    }

    public double scalarMultiply(DoubleArrayList weights) {
        double sum = 0.;
        for (int i = 0; i < categoricalIndexes.size(); i++) {
            sum += weights.getDouble(i);
        }
        for (int i = 0; i < numericalIndexes.size(); i++) {
            sum += numericalValues.getDouble(i) * weights.getDouble(i + categoricalIndexes.size());
        }
        return sum;
    }
}
