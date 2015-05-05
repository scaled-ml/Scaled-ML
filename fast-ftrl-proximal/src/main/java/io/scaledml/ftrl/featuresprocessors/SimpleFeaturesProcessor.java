package io.scaledml.ftrl.featuresprocessors;

import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.core.util.Util;

public class SimpleFeaturesProcessor implements FeaturesProcessor {

    @Override
    public void addCategoricalFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature) {
        item.addCategoricalIndex(index(namespace, feature));
    }

    private long index(LineBytesBuffer namespace, LineBytesBuffer feature) {
        return Util.murmur().newHasher()
                .putBytes(namespace.bytes(), 0, namespace.size())
                .putBytes(feature.bytes(), 0, feature.size()).hash().asLong();
    }

    @Override
    public void addNumericalFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value) {
        item.addNumericalIndex(index(namespace, feature), value);
    }

    @Override
    public void finalize(SparseItem item) {

    }
}
