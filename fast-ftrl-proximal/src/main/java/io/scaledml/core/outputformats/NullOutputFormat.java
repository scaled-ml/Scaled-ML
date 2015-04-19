package io.scaledml.core.outputformats;


import io.scaledml.core.SparseItem;

import java.io.IOException;

public class NullOutputFormat implements OutputFormat {
    @Override
    public void emit(SparseItem item, double prediction) {
    }

    @Override
    public void close() throws IOException {
    }
}
