package io.scaledml.ftrl.parallel;


import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteUpdatesEventHandler implements EventHandler<TwoPhaseEvent<Increment>> {
    private static final Logger logger = LoggerFactory.getLogger(WriteUpdatesEventHandler.class);
    private FtrlProximalModel model;


    @Override
    public void onEvent(TwoPhaseEvent<Increment> event, long sequence, boolean endOfBatch) throws Exception {
        event.output().writeToModel(model);
    }

    @Inject
    public WriteUpdatesEventHandler model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
}
