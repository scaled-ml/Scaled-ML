package io.scaledml.core.inputformats;


import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;

public class BinaryInputFormat implements InputFormat {

    @Override
    public void parse(LineBytesBuffer line, SparseItem item, long lineNo) {
        item.read(line);
    }
}
