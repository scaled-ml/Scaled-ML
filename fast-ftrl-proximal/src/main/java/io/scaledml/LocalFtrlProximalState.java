package io.scaledml;

import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;

public class LocalFtrlProximalState implements Serializable, FtrlProximalState {

    private FloatBigList n;
    private FloatBigList z;
    private transient Increment increment;

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
    public void readVectors(LongList indexes, Long2DoubleMap currentN, Long2DoubleMap currentZ) {
        currentN.clear();
        currentZ.clear();
        for (long index : indexes) {
            currentN.put(index, n.getFloat(index));
            currentZ.put(index, z.getFloat(index));
        }
    }

    @Override
    public Increment getIncrement() {
        increment.clear();
        return increment;
    }

    @Override
    public void writeIncrement() {
        for (long index : increment.incrementOfN.keySet()) {
            n.set(index, (float) (n.getFloat(index) + increment.incrementOfN.get(index)));
        }
        for (long index : increment.incrementOfZ.keySet()) {
            z.set(index, (float) (z.getFloat(index) + increment.incrementOfZ.get(index)));
        }
    }

    public void initTransientFields(int size) {
        increment = new Increment();
        increment.incrementOfN = FTRLProximalAlgorithm.createMap(size);
        increment.incrementOfZ = FTRLProximalAlgorithm.createMap(size);
    }
}
