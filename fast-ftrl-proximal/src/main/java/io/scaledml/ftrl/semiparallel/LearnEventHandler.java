package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import io.scaledml.ftrl.outputformats.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnEventHandler implements EventHandler<TwoPhaseEvent<SparseItem>> {
    private static final Logger logger = LoggerFactory.getLogger(LearnEventHandler.class);
    private OutputFormat outputFormat;
    private FTRLProximalAlgorithm algorithm;
    private FtrlProximalModel model;
    private Increment increment = new Increment();

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event, long sequence, boolean endOfBatch) throws Exception {
        double prediction = algorithm.learn(event.output(), increment);
        increment.writeToModel(model);
        outputFormat.emmit(event.output(), prediction);
    }

    @Inject
    public LearnEventHandler outputFormat(OutputFormat format) {
        this.outputFormat = format;
        return this;
    }
    @Inject
    public LearnEventHandler algorithm(FTRLProximalAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
    @Inject
    public LearnEventHandler model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }
}
