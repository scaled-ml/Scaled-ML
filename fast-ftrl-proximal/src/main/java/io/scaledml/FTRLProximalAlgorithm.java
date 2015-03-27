package io.scaledml;


import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.Serializable;

public class FTRLProximalAlgorithm implements Serializable {

    private FtrlProximalState state;
    private double lambda1;
    private double lambda2;
    private double alfa;
    private double beta;
    private DoubleArrayList currentN = new DoubleArrayList();
    private DoubleArrayList currentZ = new DoubleArrayList();
    private DoubleArrayList currentWeights = new DoubleArrayList();

    public FTRLProximalAlgorithm(long b, double lambda1, double lambda2, double alfa, double beta) {
        this();
        assert b < 64;
        long size = 1L << b;
        state = new LocalFtrlProximalState(size);
        this.alfa = alfa;
        this.beta = beta;
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
    }

    public FTRLProximalAlgorithm() {
    }

    public double train(SparseItem item) {
        calculateWeights(item);
        double predict = predict();
        double gradient = item.getLabel() - predict;

        FtrlProximalState.Increment increment = state.getIncrement();
        for (int i = 0; i < item.getIndexes().size(); i++) {
            long index = item.getIndexes().getLong(i);
            double nDelta = gradient * gradient;
            double n = currentN.getDouble(i);
            double w = currentWeights.getDouble(i);
            double learning_rate = 1. / alfa * (Math.sqrt(n + gradient * gradient) - Math.sqrt(n));
            double zDelta = gradient - w * learning_rate;
            increment.addIncrement(index, nDelta, zDelta);
        }
        state.writeIncrement();
        return predict;
    }

    public double test(SparseItem item) {
        calculateWeights(item);
        return predict();
    }

    private double predict() {
        double sumWeights = 0.;
        for (int i = 0; i < currentWeights.size(); i++) {
            sumWeights += currentWeights.getDouble(i);
        }
        return 1. / (1. + Math.exp(sumWeights));
    }

    private void calculateWeights(SparseItem item) {
        currentWeights.clear();
        state.readVectors(item.getIndexes(), currentN, currentZ);
        for (int i = 0; i < item.getIndexes().size(); i++) {
            double z = currentZ.getDouble(i);
            double n = currentN.getDouble(i);
            if (Math.abs(z) > lambda1) {
                currentWeights.add(-1. / ((beta + Math.sqrt(n)) / alfa + lambda2) * (z -
                        Math.signum(z) * lambda1));
            } else {
                currentWeights.add(0.);
            }
        }
    }


    public long featuresNum() {
        return state.size();
    }
}
