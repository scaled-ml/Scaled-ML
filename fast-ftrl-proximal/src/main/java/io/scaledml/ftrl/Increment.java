package io.scaledml.ftrl;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;


public class Increment {
    private final LongList indexes = new LongArrayList();
    private final DoubleList incrementOfN = new DoubleArrayList();
    private final DoubleList incrementOfZ = new DoubleArrayList();

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

    public DoubleList incrementOfZ() {
        return incrementOfZ;
    }

    public DoubleList incrementOfN() {
        return incrementOfN;
    }

    public LongList indexes() {
        return indexes;
    }
}
