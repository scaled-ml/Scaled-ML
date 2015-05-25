package io.scaledml.features;

import io.scaledml.core.util.Util;
import it.unimi.dsi.fastutil.doubles.*;
import java.util.Collections;

public class Binning {
    private final DoubleList percentiles = new DoubleArrayList();

    public Binning addPercentile(double percentile) {
        percentiles.add(percentile);
        return this;
    }

    public Binning finishBuild() {
        percentiles.sort(Double::compare);
        for (int i = 0; i < percentiles.size() - 1; i++) {
            if (Util.doublesEqual(percentiles.getDouble(i), percentiles.getDouble(i + 1), 0.0001)) {
                percentiles.set(i + 1, percentiles.getDouble(i));
            }
        }
        return this;
    }


    public double roundToPercentile(double value) {
        int i = getInsertionPoint(value);
        if (i >= 0) {
            return percentiles.getDouble(i);
        }
        throw new IllegalArgumentException(value + " is not a part of sample");
    }

    int getInsertionPoint(double value) {
        int i = Collections.binarySearch(percentiles, value);
        if (i >= 0) {
            return i;
        }
        return -i - 2;
    }

    public int getNumberOfValuesBetween(double from, double to) {
        return getInsertionPoint(to - Util.EPSILON) -
                getInsertionPoint(from - Util.EPSILON);
    }
}
