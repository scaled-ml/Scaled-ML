package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import io.scaledml.ftrl.outputformats.OutputFormat;

public class LearnEventHandler implements EventHandler<TwoPhaseEvent<SparseItem>> {
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
