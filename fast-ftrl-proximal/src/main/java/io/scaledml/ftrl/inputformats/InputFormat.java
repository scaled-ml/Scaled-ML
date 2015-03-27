package io.scaledml.ftrl.inputformats;

import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.io.LineBytesBuffer;

public interface InputFormat {
    void parse(LineBytesBuffer line, SparseItem item);
}
