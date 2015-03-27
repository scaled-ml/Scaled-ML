package io.scaledml.outputformats;

import io.scaledml.SparseItem;

import java.io.Closeable;

public interface OutputFormat extends Closeable {
    void emmit(SparseItem item, double prediction);
}
