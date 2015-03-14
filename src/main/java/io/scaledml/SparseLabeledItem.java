package io.scaledml;


public class SparseLabeledItem extends SparseItem {
    private float label;

    public void setLabel(float label) {
        this.label = label;
    }

    public float getLabel() {
        return label;
    }
}
