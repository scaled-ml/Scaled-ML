package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import io.scaledml.ftrl.FTRLProximalAlgorithm;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.Increment;
import io.scaledml.core.SparseItem;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import io.scaledml.core.outputformats.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

public class LearnEventHandler implements EventHandler<TwoPhaseEvent<SparseItem>>, LifecycleAware {
    private static final Logger logger = LoggerFactory.getLogger(LearnEventHandler.class);
    private OutputFormat outputFormat;
    private FTRLProximalAlgorithm algorithm;
    private FtrlProximalModel model;
    private Increment increment = new Increment();
    private Phaser phaser;

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event, long sequence, boolean endOfBatch) throws Exception {
        double prediction = algorithm.learn(event.output(), increment);
        model.writeToModel(increment);
        outputFormat.emit(event.output(), prediction);
    }

    @Override
    public void onStart() {
        phaser.register();
    }

    @Override
    public void onShutdown() {
        try {
            outputFormat.close();
            phaser.arriveAndDeregister();
        } catch (IOException e) {
            logger.error("Failed to close", e);
        }
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

    @Inject
    public LearnEventHandler phaser(Phaser phaser) {
        this.phaser = phaser;
        return this;
    }
}
