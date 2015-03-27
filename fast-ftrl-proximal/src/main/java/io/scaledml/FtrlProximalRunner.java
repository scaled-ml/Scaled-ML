package io.scaledml;


import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.scaledml.io.LineBytesBuffer;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class FtrlProximalRunner {
    Disruptor<LineBytesBuffer> inputDisruptor = new Disruptor<LineBytesBuffer>(
            LineBytesBuffer::new,
            512,
            Executors.newCachedThreadPool(),
            ProducerType.SINGLE,
            new SleepingWaitStrategy());
    Disruptor<FtrlProximalState.Increment> incrementDisruptor = new Disruptor<FtrlProximalState.Increment>(
            FtrlProximalState.Increment::new,
            8,
            Executors.newCachedThreadPool());
    private FloatBigList n;
    private FloatBigList z;

    static class TrainEventProcessor implements EventHandler<LineBytesBuffer> {
        int id;
        InputFormat format;
        FTRLProximalAlgorithm algorithm;
        RunStatistics statistics;

        @Override
        public void onEvent(LineBytesBuffer event, long sequence, boolean endOfBatch) throws Exception {
            if (sequence % id != 0) {
                return;
            }
            SparseItem parse = format.parse(event);
            double train = algorithm.train(parse);
            statistics.collectStatistics(parse, train);
        }
    }

    class TrainFrtrlPriximalState implements FtrlProximalState {
        long currentIncrementCursor;
        @Override
        public long size() {
            return n.size64();
        }

        @Override
        public void readVectors(LongList indexes, DoubleList currentN, DoubleList currentZ) {
            currentN.clear();
            currentZ.clear();
            for (long index : indexes) {
                currentN.add(n.getFloat(index));
                currentZ.add(z.getFloat(index));
            }
        }

        @Override
        public Increment getIncrement() {
            currentIncrementCursor = incrementDisruptor.getCursor();
            Increment increment = incrementDisruptor.get(currentIncrementCursor);
            increment.clear();
            return increment;
        }

        @Override
        public void writeIncrement() {
            incrementDisruptor.getRingBuffer().publish(currentIncrementCursor);
        }
    }

    public void process(InputStream is) throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(is)) {
            inputDisruptor.start();
            RingBuffer<LineBytesBuffer> ringBuffer = inputDisruptor.getRingBuffer();
            long cursor = ringBuffer.getCursor();
            LineBytesBuffer buffer = ringBuffer.get(cursor);
            while (buffer.readLineFrom(stream)) {
                ringBuffer.publish(cursor);
                cursor = ringBuffer.getCursor();
                buffer = ringBuffer.get(cursor);
            }
        } finally {
            inputDisruptor.shutdown();
        }
    }
}
