package io.scaledml.ftrl.parallel;


import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;

public class WriteUpdatesEventHandler implements EventHandler<TwoPhaseEvent<Increment>> {
    private FtrlProximalModel model;

    @Override
    public void onEvent(TwoPhaseEvent<Increment> event, long sequence, boolean endOfBatch) throws Exception {
        model.writeToModel(event.output());
    }

    @Inject
    public WriteUpdatesEventHandler model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
}
