package io.scaledml;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;

/**
 * Created by aonuchin on 24.03.15.
 */
public interface FtrlProximalState {
    long size();

    void readVectors(LongList indexes, DoubleList currentN, DoubleList currentZ);

    Increment getIncrement();

    void writeIncrement();

    public static class Increment implements Serializable {
        LongList indexes = new LongArrayList();
        DoubleList incrementOfN = new DoubleArrayList();
        DoubleList incrementOfZ = new DoubleArrayList();

        public void addIncrement(long index, double nDelta, double zDelta) {
            indexes.add(index);
            incrementOfN.add(nDelta);
            incrementOfZ.add(zDelta);
        }

        public void clear() {
            indexes.clear();
            incrementOfN.clear();
            incrementOfZ.clear();
        }
    }
}
