package io.scaledml.ftrl.outputformats;

import io.scaledml.ftrl.SparseItem;

import java.io.IOException;
import java.io.PrintStream;

public class PrintStreamOutputFormat implements OutputFormat {
    private PrintStream outputStream;

    @Override
    public void emmit(SparseItem item, double prediction) {
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
