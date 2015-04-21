package io.scaledml.core.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Doubles;

public class Util {
    private final static HashFunction murmur = Hashing.murmur3_128(42);

    private final static HashFunction murmur32 = Hashing.murmur3_32(17);

    public static boolean doublesEqual(double d1, double d2, double precision) {
        if (!Doubles.isFinite(d1) || !Doubles.isFinite(d2)) {
            return false;
        }
        return Math.abs(d1 - d2) < precision;
    }

    public static boolean doublesEqual(double d1, double d2) {
       return doublesEqual(d1, d2, 0.0000001);
    }

    public static HashFunction murmur() {
        return murmur;
    }

    public static HashFunction murmur32() {
        return murmur32;
    }
}
