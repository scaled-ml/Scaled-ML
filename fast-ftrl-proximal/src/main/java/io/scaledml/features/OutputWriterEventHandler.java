package io.scaledml.features;


import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.core.TwoPhaseEvent;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

import java.io.IOException;
import java.util.concurrent.Phaser;

public class OutputWriterEventHandler implements EventHandler<TwoPhaseEvent<SparseItem>>, LifecycleAware {
    private Phaser phaser;
    private FastBufferedOutputStream outputStream;
    private final LineBytesBuffer buffer = new LineBytesBuffer();

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event, long sequence, boolean endOfBatch) throws Exception {
        buffer.clear();
        SparseItem item = event.output();
        item.write(buffer);
        outputStream.write(buffer.bytes(), 0, buffer.size());
        outputStream.write('\n');
    }

    @Override
    public void onStart() {
        phaser.register();
    }

    @Override
    public void onShutdown() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    @Inject
    public OutputWriterEventHandler phaser(Phaser phaser) {
        this.phaser = phaser;
        return this;
    }

    @Inject
    public OutputWriterEventHandler outputStream(FastBufferedOutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }
}
