package io.scaledml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aonuchin on 14.03.15.
 */
public class SparseItem {
    private final List<Long> indexes = new ArrayList<>();

    public void addIndex(long index) {
        indexes.add(index);
    }

    public List<Long> getIndexes() {
        return indexes;
    }
}
