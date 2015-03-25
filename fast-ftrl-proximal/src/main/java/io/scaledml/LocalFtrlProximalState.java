package io.scaledml;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;

public class LocalFtrlProximalState implements Serializable, FtrlProximalState {

    private FloatBigList n;
    private FloatBigList z;
    private Increment increment = new Increment();

    public LocalFtrlProximalState(long size) {
        z = new FloatBigArrayBigList(size);
        z.size(size);
        n = new FloatBigArrayBigList(size);
        n.size(size);
    }

    @Override
    public long size() {
        return n.size64();
    }

    @Override
    public void readVectors(LongList indexes, DoubleList currentN, DoubleList currentZ) {
        currentN.clear();
        currentZ.clear();
        for (long index : indexes) {
            currentN.add(n.getFloat(index));
            currentZ.add(z.getFloat(index));
        }
    }

    @Override
    public Increment getIncrement() {
        increment.clear();
        return increment;
    }

    @Override
    public void writeIncrement() {
        for (int i = 0; i < increment.indexes.size(); i++) {
            long index = increment.indexes.getLong(i);
            double nDelta = increment.incrementOfN.getDouble(i);
            double zDelta = increment.incrementOfZ.getDouble(i);
            n.set(index, (float) (n.getFloat(index) + nDelta));
            z.set(index, (float) (z.getFloat(index) + zDelta));
        }
    }
}
