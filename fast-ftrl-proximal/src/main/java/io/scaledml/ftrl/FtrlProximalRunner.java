package io.scaledml.ftrl;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import io.scaledml.ftrl.util.LineBytesBuffer;
import io.scaledml.ftrl.outputformats.OutputFormat;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class FtrlProximalRunner {
    private Disruptor<? extends TwoPhaseEvent<?>> disruptor;
    private InputStream inputStream;
    private FtrlProximalModel model;
    private Path outputForModelPath;
    private OutputFormat outputFormat;

    public void process() throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(inputStream)) {
            disruptor.start();
            RingBuffer<? extends TwoPhaseEvent> ringBuffer = disruptor.getRingBuffer();
            long cursor = ringBuffer.next();
            LineBytesBuffer buffer = ringBuffer.get(cursor).input();
            while (buffer.readLineFrom(stream)) {
                ringBuffer.publish(cursor);
                cursor = ringBuffer.next();
                buffer = ringBuffer.get(cursor).input();
            }
            disruptor.shutdown();
        } finally {
            outputFormat.close();
        }
        if (outputForModelPath != null) {
            FtrlProximalModel.saveModel(model, outputForModelPath);
        }
    }

    @Inject
    public FtrlProximalRunner disruptor(@Named("disruptor") Disruptor<? extends TwoPhaseEvent<?>> disruptor) {
        this.disruptor = disruptor;
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
    public FtrlProximalRunner outputFormat(@Named("delegate") OutputFormat outputFormat) {
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
