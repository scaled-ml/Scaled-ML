package io.scaledml.ftrl.featuresprocessors;

import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.util.LineBytesBuffer;

public interface FeaturesProcessor {
    void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value);

    void finalize(SparseItem item);


}
