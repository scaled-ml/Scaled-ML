package io.scaledml;

import io.scaledml.io.LineBytesBuffer;

/**
 * Created by aonuchin on 24.03.15.
 */
public interface InputFormat {
    SparseItem parse(LineBytesBuffer line);
}
