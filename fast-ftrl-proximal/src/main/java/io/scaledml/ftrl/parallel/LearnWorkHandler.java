package io.scaledml.ftrl.parallel;

import com.google.inject.Inject;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import io.scaledml.ftrl.inputformats.InputFormat;
import io.scaledml.ftrl.outputformats.OutputFormat;

public class LearnWorkHandler implements WorkHandler<TwoPhaseEvent<Increment>> {
    private InputFormat inputFormat;
    private FTRLProximalAlgorithm algorithm;
    private OutputFormat outputFormat;
    private final SparseItem item = new SparseItem();

    @Override
    public void onEvent(TwoPhaseEvent<Increment> event) throws Exception {
        item.clear();
        inputFormat.parse(event.input(), item);
        outputFormat.emmit(item, algorithm.learn(item, event.output()));
    }

    @Inject
    public LearnWorkHandler inputFormat(InputFormat inputFormat) {
        this.inputFormat = inputFormat;
        return this;
    }
    @Inject
    public LearnWorkHandler algorithm(FTRLProximalAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
    @Inject
    public LearnWorkHandler outputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }
}
