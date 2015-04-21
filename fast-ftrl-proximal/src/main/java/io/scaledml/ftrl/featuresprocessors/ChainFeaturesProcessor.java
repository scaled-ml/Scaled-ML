package io.scaledml.ftrl.featuresprocessors;

import com.google.inject.Inject;
import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;

public abstract class ChainFeaturesProcessor implements FeaturesProcessor {
    private FeaturesProcessor next;

    @Override
    public void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value) {
        doAddFeature(item, namespace, feature, value);
        next.addFeature(item, namespace, feature, value);
    }

    protected abstract void doAddFeature(SparseItem item, LineBytesBuffer namespace,
                                         LineBytesBuffer feature, double value);

    @Override
    public void finalize(SparseItem item) {
        doFinalize(item);
        next.finalize(item);
    }

    protected abstract void doFinalize(SparseItem item);

    @Inject
    public <T extends ChainFeaturesProcessor> T next(FeaturesProcessor next) {
        this.next = next;
        return (T) this;
    }
}
