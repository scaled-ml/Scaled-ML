package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.google.inject.Inject;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.core.SparseItem;
import io.scaledml.core.inputformats.InputFormat;
import io.scaledml.core.TwoPhaseEvent;
import it.unimi.dsi.fastutil.longs.*;

import java.util.concurrent.Phaser;

public class StatisticsWorkHandler implements WorkHandler<TwoPhaseEvent<Void>>, LifecycleAware {
    private final Long2ObjectMap<TDigest> digests = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2DoubleMap minimums = new Long2DoubleOpenHashMap();
    private final Long2DoubleMap maximums = new Long2DoubleOpenHashMap();
    private final Long2LongMap counts = new Long2LongOpenHashMap();
    private final SparseItem item = new SparseItem();
    private NumericalFeaturesStatistics listener;
    private InputFormat format;
    private Phaser phaser;

    public StatisticsWorkHandler() {
        minimums.defaultReturnValue(Double.MAX_VALUE);
        maximums.defaultReturnValue(Double.MIN_VALUE);
        counts.defaultReturnValue(0);
    }

    @Override
    public void onEvent(TwoPhaseEvent<Void> event) throws Exception {
        item.clear();
        format.parse(event.input(), item, event.lineNo());
        for (int i = 0; i < item.numericalIndexes().size(); i++) {
            long index = item.numericalIndexes().getLong(i);
            double value = item.numericalValues().getDouble(i);
            if (!digests.containsKey(index)) {
                digests.put(index, new TDigest(100));
            }
            counts.put(index, counts.get(index) + 1);
            minimums.put(index, Math.min(minimums.get(index), value));
            maximums.put(index, Math.max(maximums.get(index), value));
            digests.get(index).add(value);
        }
    }

    @Override
    public void onStart() {
        phaser.register();
    }

    @Override
    public void onShutdown() {
        try {
            listener.finishCalculateDigests(this);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    @Inject
    public StatisticsWorkHandler listener(NumericalFeaturesStatistics listener) {
        this.listener = listener;
        return this;
    }

    @Inject
    public StatisticsWorkHandler phaser(Phaser phaser) {
        this.phaser = phaser;
        return this;
    }

    @Inject
    public StatisticsWorkHandler format(InputFormat format) {
        this.format = format;
        return this;
    }

    public Long2ObjectMap<TDigest> digests() {
        return digests;
    }

    public Long2DoubleMap minimums() {
        return minimums;
    }

    public Long2DoubleMap maximums() {
        return maximums;
    }

    public Long2LongMap counts() {
        return counts;
    }
}
