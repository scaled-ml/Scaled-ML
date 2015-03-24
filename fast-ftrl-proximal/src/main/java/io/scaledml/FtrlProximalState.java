package io.scaledml;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * Created by aonuchin on 24.03.15.
 */
public interface FtrlProximalState {
    long size();

    void readVectors(LongList indexes, Long2DoubleMap currentN, Long2DoubleMap currentZ);

    Increment getIncrement();

    void writeIncrement();

    void initTransientFields(int featuresNum);

    public static class Increment {
        Long2DoubleMap incrementOfN;
        Long2DoubleMap incrementOfZ;

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
}
