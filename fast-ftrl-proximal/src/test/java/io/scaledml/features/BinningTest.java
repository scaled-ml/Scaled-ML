package io.scaledml.features;

import org.junit.Test;

import static org.junit.Assert.*;

public class BinningTest {

    @Test
    public void testGetInsertionPoint() throws Exception {
        Binning binning = new Binning();
        for (int i = 0; i < 100; i++) {
            binning.addPercentile(i * 0.1);
        }
        binning.finishBuild();
        for (int i = 0; i < 100; i++) {
            assertEquals(i, binning.getInsertionPoint(i * 0.1));
            assertEquals(i, binning.getInsertionPoint(i * 0.1 + 0.005));
        }
    }

    @Test
    public void testGetNumberOfValuesBetween1() throws Exception {
        Binning binning = new Binning();
        for (int i = 0; i < 100; i++) {
            binning.addPercentile(i * 0.1);
        }
        binning.finishBuild();
        for (int i = 0; i < 99; i++) {
            assertEquals("i=" + i, 1, binning.getNumberOfValuesBetween(i * 0.1, i * 0.1 + 0.1));
            assertEquals("i=" + i, 1, binning.getNumberOfValuesBetween(i * 0.1 + 0.005, i * 0.1 + 0.105));
        }
        assertEquals(1, binning.getNumberOfValuesBetween(9.9, 10.));
        assertEquals(0, binning.getNumberOfValuesBetween(10., 10.1));
        assertEquals(0, binning.getNumberOfValuesBetween(-0.1, 0.));
    }
}