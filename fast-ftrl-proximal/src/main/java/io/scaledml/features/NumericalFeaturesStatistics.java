package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.clearspring.analytics.util.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.core.util.Util;
import it.unimi.dsi.fastutil.doubles.Double2DoubleArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;
import it.unimi.dsi.fastutil.longs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumericalFeaturesStatistics {
    private static final Logger statisticsLogger = LoggerFactory.getLogger("statistics-logger");
    private final Long2ObjectMap<TDigest> digests = new Long2ObjectLinkedOpenHashMap<>();
    private Long2ObjectMap<Binning> binnings = null;
    private Long2ObjectMap<Double2DoubleMap> histograms = null;
    private double percentsBinningStep;
    private final Long2DoubleMap minimums = new Long2DoubleLinkedOpenHashMap();
    private final Long2DoubleMap maximums = new Long2DoubleLinkedOpenHashMap();
    private final Long2LongMap counts = new Long2LongLinkedOpenHashMap();

    public NumericalFeaturesStatistics() {
        maximums.defaultReturnValue(Double.MIN_VALUE);
        minimums.defaultReturnValue(Double.MAX_VALUE);
        counts.defaultReturnValue(0);
    }

    public synchronized void finishCalculateDigests(StatisticsWorkHandler handler) {
        for (long index : handler.digests().keySet()) {
            if (!digests.containsKey(index)) {
                digests.put(index, handler.digests().get(index));
            } else {
                digests.get(index).add(handler.digests().get(index));
            }
        }
        for (long index : handler.counts().keySet()) {
            long count = handler.counts().get(index);
            counts.put(index, counts.get(index) + count);
        }
        for (long index : handler.minimums().keySet()) {
            double min = handler.minimums().get(index);
            minimums.put(index, Math.min(minimums.get(index), min));
        }
        for (long index : handler.maximums().keySet()) {
            double max = handler.maximums().get(index);
            maximums.put(index, Math.max(maximums.get(index), max));
        }
    }

    private synchronized Long2ObjectMap<Binning> buildBinning() {
        Long2ObjectMap<Binning> newBinnings = new Long2ObjectLinkedOpenHashMap<>();
        for (long index : digests.keySet()) {
            TDigest digest = digests.get(index);
            double min = minimums.get(index);
            Binning binning = buildBinning(digest, min);
            newBinnings.put(index, binning);
        }
        return newBinnings;
    }

    Binning buildBinning(TDigest digest, double min) {
        Binning binning = new Binning().addPercentile(min);
        for (double p = percentsBinningStep; Util.doublesLess(p, 1.); p += percentsBinningStep) {
            binning.addPercentile(digest.quantile(p));
        }
        binning.finishBuild();
        return binning;
    }

    public Long2ObjectMap<Binning> binnings() {
        if (binnings == null) {
            binnings = buildBinning();
        }
        return binnings;
    }

    public synchronized Long2ObjectMap<Double2DoubleMap> buildHistograms() {
        Long2ObjectMap<Double2DoubleMap> histograms = new Long2ObjectLinkedOpenHashMap<>();
        for (long index : binnings().keySet()) {
            Binning binning = binnings().get(index);
            double max = maximums.get(index);
            double min = minimums.get(index);
            Double2DoubleMap histogram = buildHistogram(binning, min, max);
            histograms.put(index, histogram);
        }
        return histograms;
    }

    Double2DoubleMap buildHistogram(Binning binning, double min, double max) {
        double step = (max - min) / 100;
        Double2DoubleMap histogram = new Double2DoubleArrayMap();
        for (double bucketLower = min - step; bucketLower < max + step; bucketLower += step) {
            int v = binning.getNumberOfValuesBetween(bucketLower, bucketLower + step);
            histogram.put(bucketLower, v * percentsBinningStep);
        }
        return histogram;
    }

    public Long2ObjectMap<Double2DoubleMap> histograms() {
        if (histograms == null) {
            histograms = buildHistograms();
        }
        return histograms;
    }

    public void logFeaturesStatistics() {
        for (long index : counts.keySet()) {
            statisticsLogger.info("Column index: " + index);
            statisticsLogger.info(
                            index + "\tCount=" + counts.get(index) +
                            "\tMax=" + maximums.get(index) +
                            "\tMin=" + minimums.get(index));
            Double2DoubleMap histogram = histograms().get(index);
            statisticsLogger.info("Statistics_type\tindex\tvalue\tdensity");
            for (double value : histogram.keySet()) {
                statisticsLogger.info("histogram\t" + index + "\t" + value + "\t" + histogram.get(value));
            }
        }
    }

    @Inject
    public NumericalFeaturesStatistics percentsHistogramStep(
            @Named("percentsBinningStep") double percentsHistogramStep) {
        this.percentsBinningStep = percentsHistogramStep;
        return this;
    }
}
