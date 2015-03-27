package io.scaledml.ftrl;


import com.google.inject.Inject;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import com.google.inject.name.Named;

public class FTRLProximalAlgorithm {
    private FtrlProximalModel model;
    private FtrlProximalModelUpdater modelUpdater;
    private boolean testOnly;
    private final DoubleArrayList currentN = new DoubleArrayList();
    private final DoubleArrayList currentZ = new DoubleArrayList();
    private final DoubleArrayList currentWeights = new DoubleArrayList();

    public double learn(SparseItem item) {
        calculateWeights(item);
        double predict = predict();
        double gradient = item.label() - predict;
        if (!testOnly) {
            FtrlProximalModelUpdater.Increment increment = modelUpdater.getIncrement();
            for (int i = 0; i < item.indexes().size(); i++) {
                long index = item.indexes().getLong(i);
                double nDelta = gradient * gradient;
                double n = currentN.getDouble(i);
                double w = currentWeights.getDouble(i);
                double learning_rate = 1. / model.alfa() * (Math.sqrt(n + gradient * gradient) - Math.sqrt(n));
                double zDelta = gradient - w * learning_rate;
                increment.addIncrement(index, nDelta, zDelta);
            }
            modelUpdater.writeIncrement();
        }
        return predict;
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
        model.readVectors(item.indexes(), currentN, currentZ);
        for (int i = 0; i < item.indexes().size(); i++) {
            double z = currentZ.getDouble(i);
            double n = currentN.getDouble(i);
            if (Math.abs(z) > model.lambda1()) {
                currentWeights.add(-1. / ((model.beta() + Math.sqrt(n)) / model.alfa() + model.lambda2()) * (z -
                        Math.signum(z) * model.lambda1()));
            } else {
                currentWeights.add(0.);
            }
        }
    }

    @Inject
    public FTRLProximalAlgorithm modelUpdater(FtrlProximalModelUpdater modelUpdater) {
        this.modelUpdater = modelUpdater;
        return this;
    }
    @Inject
    public FTRLProximalAlgorithm model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
    @Inject
    public FTRLProximalAlgorithm testOnly(@Named("testOnly") boolean testOnly) {
        this.testOnly = testOnly;
        return this;
    }
}
