package io.scaledml.ftrl.inputformats;

import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.util.LineBytesBuffer;

public interface FeatruresProcessor {
    void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value);

    void finalize(SparseItem item);


}
