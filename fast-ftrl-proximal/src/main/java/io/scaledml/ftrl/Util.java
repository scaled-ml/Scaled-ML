package io.scaledml.ftrl;

import com.google.common.primitives.Doubles;

public class Util {
    private static final double EPSILON = 0.000001;

    public static boolean doublesEqual(double d1, double d2) {
        if (!Doubles.isFinite(d1) || !Doubles.isFinite(d2)) {
            return false;
        }
        return Math.abs(d1 - d2) < EPSILON;
    }
}
