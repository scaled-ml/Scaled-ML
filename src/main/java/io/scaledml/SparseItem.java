package io.scaledml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SparseItem implements Serializable {
    private final List<Long> indexes = new ArrayList<>();
    private float label;

    public void addIndex(long index) {
        indexes.add(index);
    }

    public List<Long> getIndexes() {
        return indexes;
    }

    public void setLabel(float label) {
        this.label = label;
    }

    public float getLabel() {
        return label;
    }
}
