package io.scaledml.features;


import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.scaledml.core.SparseItem;
import io.scaledml.core.inputformats.*;
import io.scaledml.core.TwoPhaseEvent;
import io.scaledml.ftrl.featuresprocessors.FeaturesProcessor;
import io.scaledml.ftrl.options.FtrlOptions;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.function.Supplier;

public class FeatureEngineeringModule  extends AbstractModule {
    protected final ExecutorService threadsProvider = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    protected final FtrlOptions options;

    public FeatureEngineeringModule(FtrlOptions options) {
        this.options = options;
    }

    @Override
    protected void configure() {
        ThrowingProviderBinder.forModule(this);
        bindConstant().annotatedWith(Names.named("testOnly")).to(options.testOnly());
        bindConstant().annotatedWith(Names.named("skipFirst")).to(options.skipFirst());
        bindConstant().annotatedWith(Names.named("percentsBinningStep")).to(0.01);
        switch (options.format()) {
            case vw:
                bind(InputFormat.class).to(VowpalWabbitFormat.class);
                break;
            case csv:
                ColumnsMask columnsMask = new ColumnsMask(options.csvMask());
                bindConstant().annotatedWith(Names.named("csvDelimiter")).to(options.csvDelimiter());
                bind(new TypeLiteral<ColumnsMask>() {
                }).annotatedWith(Names.named("csvMask")).toInstance(columnsMask);
                bind(InputFormat.class).to(CSVFormat.class);
                break;
            case binary:
                bind(InputFormat.class).to(BinaryInputFormat.class);
                break;
            default:
                throw new IllegalArgumentException(options.format().toString());
        }
        bind(FirstPassRunner.class);
        bind(SecondPassRunner.class);
        bind(FeatureEngineeringRunner.class);
        bind(Phaser.class).asEagerSingleton();
        bind(FeaturesProcessor.class);
        try {
            bind(new TypeLiteral<Supplier<InputStream>>() {}).toInstance(this::openInputFile);
            bind(FastBufferedOutputStream.class).toInstance(new FastBufferedOutputStream(
                    Files.newOutputStream(Paths.get(options.predictions()))));

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        bind(NumericalFeaturesStatistics.class).asEagerSingleton();
        bind(new TypeLiteral<WorkHandler<TwoPhaseEvent<Void>>>() {
        })
                .to(StatisticsWorkHandler.class);
        bind(new TypeLiteral<WorkHandler<TwoPhaseEvent<SparseItem>>>() {
        })
                .to(BinningWorkHandler.class);
        bind(new TypeLiteral<EventHandler<TwoPhaseEvent<SparseItem>>>() {})
                .to(OutputWriterEventHandler.class);
    }

    @Provides
    @Singleton
    @Named("firstPassDisruptor")
    public Disruptor<TwoPhaseEvent<Void>> firstPassDisruptor(
            Provider<WorkHandler<TwoPhaseEvent<Void>>> statisticsHandlerProvider) {
        Disruptor<TwoPhaseEvent<Void>> disruptor = new Disruptor<>(
                TwoPhaseEvent.factory(() -> null),
                options.ringSize(), threadsProvider,
                ProducerType.SINGLE, new SleepingWaitStrategy());
        WorkHandler<TwoPhaseEvent<Void>>[] parsers = new WorkHandler[options.threads()];
        for (int i = 0; i < options.threads(); i++) {
            parsers[i] = statisticsHandlerProvider.get();
        }
        disruptor.handleExceptionsWith(new FatalExceptionHandler());
        disruptor.handleEventsWithWorkerPool(parsers);
        return disruptor;
    }

    @Provides
    @Singleton
    @Named("secondPassDisruptor")
    public Disruptor<TwoPhaseEvent<SparseItem>> secondPassDisruptor(
            Provider<WorkHandler<TwoPhaseEvent<SparseItem>>> binningHandlerProvider,
            Provider<EventHandler<TwoPhaseEvent<SparseItem>>> outputHandlerProvider) {
        Disruptor<TwoPhaseEvent<SparseItem>> disruptor = new Disruptor<>(
                TwoPhaseEvent.factory(SparseItem::new),
                options.ringSize(), threadsProvider,
                ProducerType.SINGLE, new SleepingWaitStrategy());
        WorkHandler<TwoPhaseEvent<SparseItem>>[] parsers = new WorkHandler[options.threads()];
        for (int i = 0; i < options.threads(); i++) {
            parsers[i] = binningHandlerProvider.get();
        }
        disruptor.handleExceptionsWith(new FatalExceptionHandler());
        disruptor.handleEventsWithWorkerPool(parsers)
                .then(outputHandlerProvider.get());
        return disruptor;
    }

    private InputStream openInputFile() {
        try {
            return Files.newInputStream(Paths.get(options.data()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
