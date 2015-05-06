package io.scaledml.core.outputformats;

import io.scaledml.core.SparseItem;

import java.io.IOException;
import java.io.PrintStream;

public class PrintStreamOutputFormat implements OutputFormat {
    private PrintStream outputStream;

    @Override
    public void emit(SparseItem item, double prediction) {
        outputStream.print(prediction + "\n");
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    public PrintStreamOutputFormat outputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }
}
