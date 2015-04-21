package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.core.SparseItem;
import io.scaledml.core.inputformats.ColumnsMask;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class CalculateDigestWorkHandler implements WorkHandler<TwoPhaseEvent<SparseItem>> {
    private final ObjectList<TDigest> digests = new ObjectArrayList<>();

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event) throws Exception {
        SparseItem item = event.output();

    }
}
