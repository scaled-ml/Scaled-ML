package io.scaledml;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SparseItem implements Serializable, Externalizable {
    private final LongList indexes = new LongArrayList();
    private double label;
    private int bytesOfHashNum;
    private ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[bytesOfHashNum];
        }
    };

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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(label);
        out.write(indexes.size());
        for (long index : indexes) {
            out.writeLong(index);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
