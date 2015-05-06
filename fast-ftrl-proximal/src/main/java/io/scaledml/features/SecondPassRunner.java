package io.scaledml.features;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.core.BaseDisruptorRunner;
import io.scaledml.core.SparseItem;
import io.scaledml.core.TwoPhaseEvent;

import java.io.IOException;

public class SecondPassRunner extends BaseDisruptorRunner {
    @Override
    protected void afterDisruptorProcessed() throws IOException {
    }

    @Inject
    public SecondPassRunner disruptor(@Named("secondPassDisruptor") Disruptor<TwoPhaseEvent<SparseItem>> disruptor) {
        super.setDisruptor(disruptor);
        return this;
    }
}
