package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.outputformats.OutputFormat;

public class LearnEventHandler implements EventHandler<SparseItem> {
    OutputFormat outputFormat;
    FTRLProximalAlgorithm algorithm;

    @Override
    public void onEvent(SparseItem item, long sequence, boolean endOfBatch) throws Exception {
        double prediction = algorithm.learn(item);
        outputFormat.emmit(item, prediction);
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
}
