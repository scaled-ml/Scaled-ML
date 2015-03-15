package io.scaledml;


import it.unimi.dsi.fastutil.BigArrays;
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
    }

    public FTRLProximal() {
    }
    public double train(SparseItem item) {
        calculateWeights(item);
        double predict = predict();
        double gradient = predict - item.getLabel();
        for (long index : item.getIndexes()) {
            if (weights.containsKey(index)) {
                double learning_rate = 1. / alfa * (Math.sqrt(n.get(index) + gradient * gradient) - Math.sqrt(n.get(index)));
                z.set(index, (float) (z.get(index) + gradient - weights.get(index) * learning_rate));
            } else {
                z.set(index, (float) (z.get(index) + gradient));
            }
            n.set(index, (float) (n.get(index) + gradient * gradient));
        }
        return predict;
    }

    private double predict() {
        return 1. / (1. + Math.exp(weights.values().stream().mapToDouble(Double::valueOf).sum()));
    }

    private void calculateWeights(SparseItem item) {
        weights.clear();
        for (long index : item.getIndexes()) {
            if (z.get(index) > lambda1) {
                weights.put(index, -1. / ((beta + Math.sqrt(n.get(index))) / alfa + lambda2) * (z.get(index) -
                        Math.signum(z.get(index)) * lambda1));
            }
        }
    }

    public double test(SparseItem item) {
        calculateWeights(item);
        return Math.log(predict()) - Math.log(1. - predict());
    }

    public void setLambda1(double lambda1) {
        this.lambda1 = lambda1;
    }

    public void setLambda2(double lambda2) {
        this.lambda2 = lambda2;
    }

    public void setAlfa(double alfa) {
        this.alfa = alfa;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public long featuresNum() {
        return z.size64();
    }
}
