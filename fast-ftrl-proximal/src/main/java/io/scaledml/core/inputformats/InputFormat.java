package io.scaledml.core.inputformats;

import io.scaledml.core.SparseItem;
import io.scaledml.ftrl.util.LineBytesBuffer;

public interface InputFormat {
    void parse(LineBytesBuffer line, SparseItem item, long lineNo);
}
