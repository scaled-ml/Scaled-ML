package io.scaledml.ftrl.outputformats;

import io.scaledml.ftrl.SparseItem;

import java.io.Closeable;

public interface OutputFormat extends Closeable {
    void emit(SparseItem item, double prediction);
}
