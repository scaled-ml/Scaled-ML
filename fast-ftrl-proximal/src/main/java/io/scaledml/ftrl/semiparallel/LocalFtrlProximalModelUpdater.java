package io.scaledml.ftrl.semiparallel;


import com.google.inject.Inject;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.FtrlProximalModelUpdater;

public class LocalFtrlProximalModelUpdater implements FtrlProximalModelUpdater {
    private final Increment increment = new Increment();
    private FtrlProximalModel model;

    @Override
    public Increment getIncrement() {
        increment.clear();
        return increment;
    }

    @Override
    public void writeIncrement() {
        for (int i = 0; i < increment.indexes().size(); i++) {
            long index = increment.indexes().getLong(i);
            double nDelta = increment.incrementOfN().getDouble(i);
            double zDelta = increment.incrementOfZ().getDouble(i);
            model.n().set(index, (float) (model.n().getFloat(index) + nDelta));
            model.z().set(index, (float) (model.z().getFloat(index) + zDelta));
        }
    }

    @Inject
    public LocalFtrlProximalModelUpdater model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
}
