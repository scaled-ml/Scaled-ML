package io.scaledml.ftrl.outputformats;


import io.scaledml.ftrl.SparseItem;

import java.io.IOException;

public class NullOutputFormat implements OutputFormat {
    @Override
    public void emit(SparseItem item, double prediction) {
    }

    @Override
    public void close() throws IOException {
    }
}
