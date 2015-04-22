package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.core.SparseItem;
import io.scaledml.core.inputformats.ColumnsMask;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.Closeable;

public class CalculateDigestWorkHandler implements WorkHandler<TwoPhaseEvent<SparseItem>>, LifecycleAware {
    private final Long2ObjectMap<TDigest> digests = new Long2ObjectLinkedOpenHashMap<>();

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event) throws Exception {
        SparseItem item = event.output();
        for (int i = 0; i < item.numericalIndexes().size(); i++) {
            long index = item.numericalIndexes().getLong(i);
            double value = item.numericalValues().getDouble(i);
            if (!digests.containsKey(index)) {
                digests.put(index, new TDigest(0.7));
            }
            digests.get(index).add(value);
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onShutdown() {

    }
}
