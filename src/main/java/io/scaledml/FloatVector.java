package io.scaledml;

import java.io.Serializable;

public class FloatVector implements Serializable {
    private float[][] elements;
    private long size;
    public FloatVector(long size) {
        this.size = size;
        int arraysNum = (int) (size / Integer.MAX_VALUE);
        elements = new float[arraysNum + 1][];
        for (int i = 0; i < arraysNum; i++) {
            elements[i] = new float[Integer.MAX_VALUE];
        }
        elements[arraysNum] = new float[(int) (size % Integer.MAX_VALUE)];
    }

    public float get(long i) {
        return elements[(int) (i / Integer.MAX_VALUE)][(int) (i % Integer.MAX_VALUE)];
    }

    public float set(long i, float value) {
        return elements[(int) (i / Integer.MAX_VALUE)][(int) (i % Integer.MAX_VALUE)] = value;
    }

    public long getSize() {
        return size;
    }
}
