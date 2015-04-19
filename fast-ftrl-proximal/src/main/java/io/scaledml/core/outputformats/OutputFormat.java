package io.scaledml.core.outputformats;

import io.scaledml.core.SparseItem;

import java.io.Closeable;

public interface OutputFormat extends Closeable {
    void emit(SparseItem item, double prediction);
}
