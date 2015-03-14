package io.scaledml;


import java.util.HashMap;

public class FTRLProximal {
    private FloatVector z;
    private FloatVector n;
    private double lambda1;
    private double lambda2;
    private double alfa;
    private double beta;
    private HashMap<Long, Float> weights = new HashMap<>();

    public FTRLProximal(long b, double lambda1, double lambda2, double alfa, double beta) {
        assert b < 64;
        long size = 2L << b;
        z = new FloatVector(size);
        n = new FloatVector(size);
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
        this.alfa = alfa;
        this.beta = beta;
    }

    public FTRLProximal() {
    }

    public void learnItem(SparseLabeledItem item) {
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
    }

    private double predict() {
        return 1. / (1. + Math.exp(weights.values().stream().mapToDouble(Float::doubleValue).sum()));
    }

    private void calculateWeights(SparseItem item) {
        weights.clear();
        for (long index : item.getIndexes()) {
            if (z.get(index) > lambda1) {
                weights.put(index, (float) (-1. / ((beta + Math.sqrt(n.get(index))) / alfa + lambda2) * (z.get(index) -
                        Math.signum(z.get(index)) * lambda1)));
            }
        }
    }

    public double apply(SparseItem item) {
        calculateWeights(item);
        return Math.log(predict()) - Math.log(1. - predict());
    }
}
