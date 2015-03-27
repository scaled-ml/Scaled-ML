package io.scaledml.inputformats;

import io.scaledml.SparseItem;
import io.scaledml.io.LineBytesBuffer;

public interface InputFormat {
    void parse(LineBytesBuffer line, SparseItem item);
}
