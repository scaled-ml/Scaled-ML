package io.scaledml.ftrl.parallel;

import com.google.inject.Inject;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.Increment;
import io.scaledml.core.SparseItem;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import io.scaledml.core.inputformats.InputFormat;
import io.scaledml.core.outputformats.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

public class LearnWorkHandler implements WorkHandler<TwoPhaseEvent<Increment>>, LifecycleAware {
    private static final Logger logger = LoggerFactory.getLogger(LearnWorkHandler.class);
    private InputFormat inputFormat;
    private FTRLProximalAlgorithm algorithm;
    private OutputFormat outputFormat;
    private final SparseItem item = new SparseItem();
    private Phaser phaser;

    @Override
    public void onEvent(TwoPhaseEvent<Increment> event) throws Exception {
        item.clear();
        inputFormat.parse(event.input(), item, event.lineNo());
        outputFormat.emit(item, algorithm.learn(item, event.output()));
    }

    @Override
    public void onStart() {
        phaser.register();
    }

    @Override
    public void onShutdown() {
        try {
            outputFormat.close();
            phaser.arrive();
        } catch (IOException e) {
            logger.error("failed to close", e);
        }
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

    @Inject
    public LearnWorkHandler phaser(Phaser phaser) {
        this.phaser = phaser;
        return this;
    }
}
