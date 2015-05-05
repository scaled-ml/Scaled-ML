package io.scaledml.features;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.core.BaseDisruptorRunner;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;

import java.io.IOException;

public class FirstPassRunner extends BaseDisruptorRunner {
    @Override
    protected void afterDisruptorProcessed() throws IOException {
    }

    @Inject
    public FirstPassRunner disruptor(@Named("firstPassDisruptor") Disruptor<TwoPhaseEvent<Void>> disruptor) {
        setDisruptor(disruptor);
        return this;
    }
}
