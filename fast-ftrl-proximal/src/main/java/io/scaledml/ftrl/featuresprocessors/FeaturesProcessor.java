package io.scaledml.ftrl.featuresprocessors;

import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;

public interface FeaturesProcessor {
    void addCategoricalFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature);
    void addNumericalFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value);
    void finalize(SparseItem item);


}
