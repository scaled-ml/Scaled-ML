package io.scaledml.ftrl.featuresprocessors;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.util.LineBytesBuffer;
import io.scaledml.ftrl.util.PoolingMultiMap;
import io.scaledml.ftrl.util.Util;

public class QuadraticFeaturesProcessor extends ChainFeaturesProcessor {

    private long featuresNumber;

    private PoolingMultiMap<LineBytesBuffer, LineBytesBuffer> featuresPerNamespace =
            new PoolingMultiMap<>(LineBytesBuffer::new, LineBytesBuffer::new, new LineBytesBuffer[0]);

    @Override
    protected void doAddFeature(SparseItem item, LineBytesBuffer namespace,
                                LineBytesBuffer feature, double value) {
        for (LineBytesBuffer eachNamespace : featuresPerNamespace.keys()) {
            if (eachNamespace.equals(namespace)) {
                continue;
            }
            int namespaceCompare = namespace.compareTo(eachNamespace);
            LineBytesBuffer namespace1 = namespaceCompare < 0 ? namespace : eachNamespace;
            LineBytesBuffer namespace2 = namespaceCompare < 0 ? eachNamespace : namespace;
            for (LineBytesBuffer eachFeature : featuresPerNamespace.getValues(eachNamespace)) {
                LineBytesBuffer feature1 = namespaceCompare < 0 ? feature : eachFeature;
                LineBytesBuffer feature2 = namespaceCompare < 0 ? eachFeature : feature;
                item.addIndex(calculateHash(namespace1, namespace2, feature1, feature2));
            }
        }
        featuresPerNamespace.appendNextValue(namespace).setContentOf(feature);
    }

    private long calculateHash(LineBytesBuffer namespace1, LineBytesBuffer namespace2,
                               LineBytesBuffer feature1, LineBytesBuffer feature2) {
        return Math.abs(Util.murmur().newHasher()
                .putBytes(namespace1.bytes(), 0, namespace1.size())
                .putBytes(namespace2.bytes(), 0, namespace2.size())
                .putBytes(feature1.bytes(), 0, feature1.size())
                .putBytes(feature2.bytes(), 0, feature2.size())
                .hash().asLong()) % featuresNumber;
    }

    @Override
    protected void doFinalize(SparseItem item) {
        featuresPerNamespace.clear();
    }

    @Inject
    public QuadraticFeaturesProcessor featuresNumber(@Named("featuresNumber") long featuresNumber) {
        this.featuresNumber = featuresNumber;
        return this;
    }
}
