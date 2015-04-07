package io.scaledml.ftrl.parallel;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.disruptor.AbstractParallelModule;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import io.scaledml.ftrl.options.FtrlOptions;
import io.scaledml.ftrl.outputformats.OutputFormat;
import io.scaledml.ftrl.outputformats.StatisticsCalculator;


public class ParallelModule extends AbstractParallelModule<Increment> {

    public ParallelModule(FtrlOptions options) {
        super(options);
    }

    @Override
    protected void configure() {
        configureCommonBeans();
        bindConstant().annotatedWith(Names.named("statsCollectors")).to(options.threads());
        bind(new TypeLiteral<EventHandler<TwoPhaseEvent<Increment>>>() {
        }).to(WriteUpdatesEventHandler.class).asEagerSingleton();
        bind(new TypeLiteral<WorkHandler<TwoPhaseEvent<Increment>>>() {
        }).to(LearnWorkHandler.class);
        bind(OutputFormat.class).to(StatisticsCalculator.class);
    }

    @Override
    protected EventFactory<Increment> outputEventFactory() {
        return Increment::new;
    }
}
