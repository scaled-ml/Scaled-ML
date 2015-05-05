package io.scaledml.core;

import com.clearspring.analytics.util.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Phaser;


public abstract class BaseDisruptorRunner {
    private Disruptor<? extends TwoPhaseEvent<?>> disruptor;
    private InputStream inputStream;
    private boolean skipFirst;
    private Phaser phaser;

    public void process() throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(inputStream)) {
            Preconditions.checkArgument(phaser.getRegisteredParties() == 0);
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
            phaser.arriveAndDeregister();
        } finally {
            afterDisruptorProcessed();
        }
    }

    protected abstract void afterDisruptorProcessed() throws IOException;

    protected void setDisruptor(Disruptor<? extends TwoPhaseEvent<?>> disruptor) {
        this.disruptor = disruptor;
    }

    @Inject
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Inject
    public void setSkipFirst(@Named("skipFirst") boolean skipFirst) {
        this.skipFirst = skipFirst;
    }

    @Inject
    public void setPhaser(Phaser phaser) {
        this.phaser = phaser;
    }
}
