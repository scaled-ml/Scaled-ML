package io.scaledml;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.io.LineBytesBuffer;
import io.scaledml.outputformats.OutputFormat;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class FtrlProximalRunner {
    private Disruptor<LineBytesBuffer> inputDisruptor;
    private Disruptor<?> secondDisruptor;
    private InputStream inputStream;
    private FtrlProximalModel model;
    private Path outputForModelPath;
    private OutputFormat outputFormat;

    public void process() throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(inputStream)) {
            inputDisruptor.start();
            secondDisruptor.start();
            RingBuffer<LineBytesBuffer> ringBuffer = inputDisruptor.getRingBuffer();
            long cursor = ringBuffer.next();
            LineBytesBuffer buffer = ringBuffer.get(cursor);
            while (buffer.readLineFrom(stream)) {
                ringBuffer.publish(cursor);
                cursor = ringBuffer.next();
                buffer = ringBuffer.get(cursor);
            }
            inputDisruptor.shutdown();
            secondDisruptor.shutdown();

        } finally {
            try (OutputFormat of = outputFormat) {}
        }
        if (outputForModelPath != null) {
            FtrlProximalModel.saveModel(model, outputForModelPath);
        }
    }

    @Inject
    public FtrlProximalRunner inputDisruptor(@Named("inputDisruptor") Disruptor<LineBytesBuffer> inputDisruptor) {
        this.inputDisruptor = inputDisruptor;
        return this;
    }
    @Inject
    public FtrlProximalRunner secondDisruptor(@Named("secondDisruptor") Disruptor<?> secondDisruptor) {
        this.secondDisruptor = secondDisruptor;
        return this;
    }
    @Inject
    public FtrlProximalRunner inputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }
    @Inject
    public FtrlProximalRunner model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
    @Inject
    public FtrlProximalRunner outputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }
    @Inject
    public FtrlProximalRunner outputForModelPath(Optional<Path> outputForModelPath) {
        return outputForModelPath(outputForModelPath.orElse(null));
    }

    public FtrlProximalRunner outputForModelPath(Path outputForModelPath) {
        this.outputForModelPath = outputForModelPath;
        return this;
    }
}
