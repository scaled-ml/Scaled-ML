package io.scaledml;

import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;

public class FtrlProximalState implements Serializable {
    void initTransientFields(int size) {
        increment = new Increment();
        increment.incrementOfN = FTRLProximal.createMap(size);
        increment.incrementOfZ = FTRLProximal.createMap(size);
    }

    static class Increment {
        private Long2DoubleMap incrementOfN;
        private Long2DoubleMap incrementOfZ;

        public void incrementN(long index, double increment) {
            incrementOfN.put(index, increment);
        }

        public void incrementZ(long index, double increment) {
            incrementOfZ.put(index, increment);
        }

        public void clear() {
            incrementOfN.clear();
            incrementOfZ.clear();
        }
    }

    private FloatBigList n;
    private FloatBigList z;
    private transient Increment increment;

    public FtrlProximalState(long size) {
        z = new FloatBigArrayBigList(size);
        z.size(size);
        n = new FloatBigArrayBigList(size);
        n.size(size);
    }

    public long size() {
        return n.size64();
    }

    public void readVectors(LongList indexes, Long2DoubleMap currentN, Long2DoubleMap currentZ) {
        currentN.clear();
        currentZ.clear();
        for (long index : indexes) {
            currentN.put(index, n.getFloat(index));
            currentZ.put(index, z.getFloat(index));
        }
    }

    public Increment getIncrement() {
        increment.clear();
        return increment;
    }

    public void writeIncrement() {
        for (long index : increment.incrementOfN.keySet()) {
            n.set(index, (float) (n.getFloat(index) + increment.incrementOfN.get(index)));
        }
        for (long index : increment.incrementOfZ.keySet()) {
            z.set(index, (float) (z.getFloat(index) + increment.incrementOfZ.get(index)));
        }
    }
}
