package io.scaledml;


public interface RunStatistics {

    void collectStatistics(SparseItem item, double predict);
}
