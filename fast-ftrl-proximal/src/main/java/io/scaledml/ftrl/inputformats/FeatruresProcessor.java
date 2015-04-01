package io.scaledml.ftrl.inputformats;

import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.io.LineBytesBuffer;

/**
 * Created by aonuchin on 01.04.15.
 */
public interface FeatruresProcessor {
    void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value);

    void finalize(SparseItem item);
}
