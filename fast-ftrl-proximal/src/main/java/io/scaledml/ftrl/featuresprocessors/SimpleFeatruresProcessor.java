package io.scaledml.ftrl.featuresprocessors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.util.LineBytesBuffer;
import io.scaledml.ftrl.util.Util;

public class SimpleFeatruresProcessor implements FeatruresProcessor {
    private long featuresNumber;

    @Override
    public void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value) {
        item.addIndex(Math.abs(Util.murmur().newHasher()
                .putBytes(namespace.bytes(), 0, namespace.size())
                .putBytes(feature.bytes(), 0, feature.size()).hash().asLong()) % featuresNumber);
    }

    @Inject
    public SimpleFeatruresProcessor featuresNumber(@Named("featuresNumber") long featuresNumber) {
        this.featuresNumber = featuresNumber;
        return this;
    }

    @Override
    public void finalize(SparseItem item) {

    }
}
