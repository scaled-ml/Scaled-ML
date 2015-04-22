package io.scaledml.ftrl;


import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import io.scaledml.core.outputformats.OutputFormat;
import io.scaledml.core.util.LineBytesBuffer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class FtrlProximalRunner {
    private Disruptor<? extends TwoPhaseEvent<?>> disruptor;
    private InputStream inputStream;
    private FtrlProximalModel model;
    private Path outputForModelPath;
    private OutputFormat outputFormat;
    private boolean skipFirst;
    private Phaser phaser;

    public void process() throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(inputStream)) {
            phaser.register();
            disruptor.start();
            RingBuffer<? extends TwoPhaseEvent> ringBuffer = disruptor.getRingBuffer();
            long cursor = ringBuffer.next();
            LineBytesBuffer buffer = ringBuffer.get(cursor).input();
            long lineNo = 0;
            ringBuffer.get(cursor).lineNo(lineNo);
            boolean needToSkipNext = skipFirst;
            while (buffer.readLineFrom(stream)) {
                if (needToSkipNext) {
                    needToSkipNext = false;
                    continue;
                }
                lineNo++;
                ringBuffer.publish(cursor);
                cursor = ringBuffer.next();
                buffer = ringBuffer.get(cursor).input();
                ringBuffer.get(cursor).lineNo(lineNo);
            }
            disruptor.shutdown();
            phaser.arriveAndAwaitAdvance();
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
    public FtrlProximalRunner skipFirst(@Named("skipFirst") boolean skipFirst) {
        this.skipFirst = skipFirst;
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

    @Inject
    public FtrlProximalRunner phaser(Phaser phaser) {
        this.phaser = phaser;
        return this;
    }
}
