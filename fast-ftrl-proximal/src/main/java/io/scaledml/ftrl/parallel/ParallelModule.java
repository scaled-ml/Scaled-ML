package io.scaledml.ftrl.parallel;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.ftrl.FtrlOptions;
import io.scaledml.ftrl.Increment;
import io.scaledml.ftrl.conf.AbstractParallelModule;
import io.scaledml.ftrl.conf.TwoPhaseEvent;
import io.scaledml.ftrl.outputformats.CollectStatisticsOutputFormat;
import io.scaledml.ftrl.outputformats.OutputFormat;

public class ParallelModule extends AbstractParallelModule<Increment> {

    public ParallelModule(FtrlOptions options) {
        super(options);
    }

    @Override
    protected void configure() {
        confgureCommonBeans();
        bindConstant().annotatedWith(Names.named("statsCollectors")).to(options.threads());
        bind(new TypeLiteral<EventHandler<TwoPhaseEvent<Increment>>>() {}).to(WriteUpdatesEventHandler.class).asEagerSingleton();
        bind(new TypeLiteral<WorkHandler<TwoPhaseEvent<Increment>>>() {}).to(LearnWorkHandler.class);
        bind(OutputFormat.class).to(CollectStatisticsOutputFormat.class);
    }

    @Override
    protected EventFactory<Increment> outputEventFactory() {
        return Increment::new;
    }
}
