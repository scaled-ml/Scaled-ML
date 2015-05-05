package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.clearspring.analytics.util.Preconditions;
import io.scaledml.core.util.Util;
import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class NumericalFeaturesStatisticsTest {

    @Test
    public void testBuildBinning() {
        TDigest digest = new TDigest(100);
        for (int i = 0; i < 1000; i++) {
            digest.add(ThreadLocalRandom.current().nextDouble());
        }
        NumericalFeaturesStatistics st = new NumericalFeaturesStatistics()
                .percentsHistogramStep(0.01);
        Binning binning = st.buildBinning(digest, 0.);
        assertEquals(0, binning.getInsertionPoint(0.));
        assertEquals(-1, binning.getInsertionPoint(-0.1));
        assertEquals(99, binning.getInsertionPoint(1.));
        assertEquals(99, binning.getInsertionPoint(Double.MAX_VALUE));
        int middleInsertion = binning.getInsertionPoint(0.5);
        assertTrue("middleInsertion is " + middleInsertion, middleInsertion > 45 && middleInsertion < 55);
        int quarterInsertion = binning.getInsertionPoint(0.25);
        assertTrue("quarterInsertion is " + quarterInsertion, quarterInsertion > 20 && quarterInsertion < 30);
        int thirdQuarterInsertion = binning.getInsertionPoint(0.75);
        assertTrue("thirdQuarterInsertion is " + thirdQuarterInsertion, thirdQuarterInsertion > 70 && thirdQuarterInsertion < 80);
    }

    @Test
    public void testBuildHistogram() {
        Binning binning = new Binning();
        for (int i = 0; i < 99; i++) {
            binning.addPercentile(ThreadLocalRandom.current().nextDouble());
        }
        binning.addPercentile(0.).finishBuild();
        NumericalFeaturesStatistics st = new NumericalFeaturesStatistics()
                .percentsHistogramStep(0.01);
        Double2DoubleMap histogram = st.buildHistogram(binning, 0, 1);
        assertEquals(102, histogram.size());
        double sum = histogram.values()
                .stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1., sum, Util.EPSILON);
    }

    @Test
    public void testBuildHistogram2() {
        DoubleList sample = new DoubleArrayList();
        for (int i = 0; i < 1000; i++) {
            sample.add(ThreadLocalRandom.current().nextGaussian());
        }
        double min = sample.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
        double max = sample.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
        Binning binning = new Binning();
        sample.forEach(binning::addPercentile);
        binning.finishBuild();
        NumericalFeaturesStatistics st = new NumericalFeaturesStatistics()
                .percentsHistogramStep(0.001);
        Double2DoubleMap histogram = st.buildHistogram(binning, min, max);
        assertEquals(102, histogram.size());
        double sum = histogram.values()
                .stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1., sum, Util.EPSILON);
    }
}