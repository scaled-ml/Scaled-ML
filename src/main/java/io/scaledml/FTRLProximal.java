package io.scaledml;


import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.io.Serializable;
import java.util.HashMap;

public class FTRLProximal implements Serializable {
    private FloatBigList z;
    private FloatBigList n;
    private double lambda1;
    private double lambda2;
    private double alfa;
    private double beta;
    private Long2DoubleMap weights = new Long2DoubleOpenHashMap();

    public FTRLProximal(long b, double lambda1, double lambda2, double alfa, double beta) {
        assert b < 64;
        long size = 1L << b;
        z = new FloatBigArrayBigList(size);
        z.size(size);
        n = new FloatBigArrayBigList(size);
        n.size(size);
        this.alfa = alfa;
        this.beta = beta;
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
    }

    public FTRLProximal() {
    }

    public double train(SparseItem item) {
        calculateWeights(item);
        double predict = predict();
        double gradient = item.getLabel() - predict;
        for (long index : item.getIndexes()) {
            if (weights.containsKey(index)) {
                double learning_rate = 1. / alfa * (Math.sqrt(n.get(index) + gradient * gradient) - Math.sqrt(n.get(index)));
                z.set(index, (float) (z.getFloat(index) + gradient - weights.get(index) * learning_rate));
            } else {
                z.set(index, (float) (z.getFloat(index) + gradient));
            }
            n.set(index, (float) (n.getFloat(index) + gradient * gradient));
        }
        return predict;
    }

    private double predict() {
        return 1. / (1. + Math.exp(sum(weights.values())));
    }

    private double sum(DoubleCollection values) {
        double sum = 0.;
        for (double e : values) {
            sum += e;
        }
        return sum;
    }

    private void calculateWeights(SparseItem item) {
        weights.clear();
        for (long index : item.getIndexes()) {
            if (Math.abs(z.get(index)) > lambda1) {
                weights.put(index, -1. / ((beta + Math.sqrt(n.get(index))) / alfa + lambda2) * (z.getFloat(index) -
                        Math.signum(z.getFloat(index)) * lambda1));
            }
        }
    }

    public double test(SparseItem item) {
        calculateWeights(item);
        return predict();
    }

    public long featuresNum() {
        return z.size64();
    }
}
