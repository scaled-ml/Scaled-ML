package io.scaledml;


import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.io.Serializable;

public class FTRLProximal implements Serializable {

    private FtrlProximalState state;
    private double lambda1;
    private double lambda2;
    private double alfa;
    private double beta;
    private transient Long2DoubleMap currentN;
    private transient Long2DoubleMap currentZ;
    private transient Long2DoubleMap currentWeights;

    public FTRLProximal(long b, double lambda1, double lambda2, double alfa, double beta) {
        this();
        assert b < 64;
        long size = 1L << b;
        state = new FtrlProximalState(size);
        this.alfa = alfa;
        this.beta = beta;
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
    }

    public FTRLProximal() {

    }
    public void initTransientFields() {
        currentN = new Long2DoubleOpenHashMap();
        currentZ = new Long2DoubleOpenHashMap();
        currentWeights = new Long2DoubleOpenHashMap();
        this.state.initTransientFields();
    }

    public double train(SparseItem item) {
        state.readVectors(item.getIndexes(), currentN, currentZ);
        calculateWeights(item);
        double predict = predict();
        double gradient = item.getLabel() - predict;

        FtrlProximalState.Increment increment = state.getIncrement();
        for (long index : item.getIndexes()) {
            double n = currentN.get(index);
            if (currentWeights.containsKey(index)) {
                double learning_rate = 1. / alfa * (Math.sqrt(n + gradient * gradient) -
                        Math.sqrt(n));
                increment.incrementZ(index, gradient - currentWeights.get(index) * learning_rate);
            } else {
                increment.incrementZ(index, gradient);
            }
            increment.incrementN(index, gradient * gradient);
        }
        state.writeIncrement();
        return predict;
    }

    private double predict() {
        return 1. / (1. + Math.exp(sum(currentWeights.values())));
    }

    private double sum(DoubleCollection values) {
        double sum = 0.;
        for (double e : values) {
            sum += e;
        }
        return sum;
    }

    private void calculateWeights(SparseItem item) {
        currentWeights.clear();
        for (long index : item.getIndexes()) {
            double z = currentZ.get(index);
            double n = currentN.get(index);
            if (Math.abs(z) > lambda1) {
                currentWeights.put(index, -1. / ((beta + Math.sqrt(n)) / alfa + lambda2) * (z -
                        Math.signum(z) * lambda1));
            }
        }
    }

    public double test(SparseItem item) {
        state.readVectors(item.getIndexes(), currentN, currentZ);
        calculateWeights(item);
        return predict();
    }

    public long featuresNum() {
        return state.size();
    }
}
