package io.scaledml.outputformats;


import io.scaledml.SparseItem;

import java.io.IOException;

public class NullOutputFormat implements OutputFormat {
    @Override
    public void emmit(SparseItem item, double prediction) {
    }

    @Override
    public void close() throws IOException {
    }
}
