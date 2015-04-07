package io.scaledml.ftrl.inputformats;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.io.LineBytesBuffer;

/**
* Created by aonuchin on 01.04.15.
*/
public class SimpleFeatruresProcessor implements FeatruresProcessor {
    private final static HashFunction murmur = Hashing.murmur3_128(42);
    private long featuresNumber;


    @Override
    public void addFeature(SparseItem item, LineBytesBuffer namespace, LineBytesBuffer feature, double value) {
        item.addIndex(Math.abs(murmur.newHasher()
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
