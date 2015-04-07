package io.scaledml.ftrl.conf;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.scaledml.ftrl.FtrlOptions;
import io.scaledml.ftrl.FtrlProximalModel;
import io.scaledml.ftrl.FtrlProximalRunner;
import io.scaledml.ftrl.inputformats.*;
import io.scaledml.ftrl.outputformats.FinishCollectStatisticsListener;
import io.scaledml.ftrl.outputformats.NullOutputFormat;
import io.scaledml.ftrl.outputformats.OutputFormat;
import io.scaledml.ftrl.outputformats.PrintStreamOutputFormat;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class AbstractParallelModule<T> extends AbstractModule {
    protected final ExecutorService threadsProvider = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    protected final FtrlOptions options;

    public AbstractParallelModule(FtrlOptions options) {
        this.options = options;
    }

    @Provides
    @Singleton
    FtrlProximalModel model() throws Exception {
        if (options.initialRegressor() != null) {
            return FtrlProximalModel.loadModel(Paths.get(options.initialRegressor()));
        }
        return new FtrlProximalModel()
                .alfa(options.alfa())
                .beta(options.beta())
                .lambda1(options.l1())
                .lambda2(options.l2())
                .featuresNumber(1L << options.hashcodeBits());
    }

    @Provides
    @Named("delegate")
    @Singleton
    OutputFormat delegateOutputFormat() throws IOException {
        if (options.predictions() == null) {
            return new NullOutputFormat();
        } else if (options.predictions().equals("/dev/stdout")) {
            return new PrintStreamOutputFormat().outputStream(System.out);
        }
        return new PrintStreamOutputFormat()
                .outputStream(new PrintStream(new FastBufferedOutputStream(
                        Files.newOutputStream(Paths.get(options.predictions())))));
    }

    @Provides
    @Singleton
    InputStream inputStream() throws IOException {
        if (options.data() == null) {
            return System.in;
        }
        return Files.newInputStream(Paths.get(options.data()));
    }

    @Provides
    @Named("featuresNumber")
    public long featuresNumber(FtrlProximalModel model) {
        return model.featuresNumber();
    }

    @Provides
    @Singleton
    Optional<Path> outputForModelPath() {
        return options.finalRegressor() == null ? Optional.<Path>empty() : Optional.of(Paths.get(options.finalRegressor()));
    }

    @Provides
    @Singleton
    @Named("disruptor")
    protected Disruptor<? extends TwoPhaseEvent<?>> inputDisruptor(@Named("disruptor") Disruptor<TwoPhaseEvent<T>> disruptor) {
        return disruptor;
    }

    @Provides
    @Singleton
    @Named("disruptor")
    @SuppressWarnings("Unchecked")
    protected Disruptor<TwoPhaseEvent<T>> inputDisruptor(Provider<WorkHandler<TwoPhaseEvent<T>>> workHandlerProvider,
                                                         Provider<EventHandler<TwoPhaseEvent<T>>> evenHandlerProvider) {
        Disruptor<TwoPhaseEvent<T>> disruptor = new Disruptor<>(
                TwoPhaseEvent.factory(outputEventFactory()),
                ringBufferSize(), threadsProvider,
                ProducerType.SINGLE, new SleepingWaitStrategy());
        WorkHandler<TwoPhaseEvent<T>>[] parsers = new WorkHandler[options.threads()];
        for (int i = 0; i < options.threads(); i++) {
            parsers[i] = workHandlerProvider.get();
        }
        disruptor.handleExceptionsWith(new FatalExceptionHandler());
        disruptor.handleEventsWithWorkerPool(parsers).then(evenHandlerProvider.get());
        return disruptor;
    }

    protected abstract EventFactory<T> outputEventFactory();


    protected void configureCommonBeans() {
        ThrowingProviderBinder.forModule(this);
        bindConstant().annotatedWith(Names.named("testOnly")).to(options.testOnly());

        switch (options.format()) {
            case "vw":
                bind(InputFormat.class).to(VowpalWabbitFormat.class);
                break;
            case "csv":
                bind(InputFormat.class).to(CSVFormat.class);
                break;
            default:
                throw new IllegalArgumentException(options.format());

        }
        bind(FtrlProximalRunner.class).asEagerSingleton();
        bind(FinishCollectStatisticsListener.class).asEagerSingleton();
    }

    @Provides
    public FeatruresProcessor featruresProcessor(@Named("featuresNumber") long featuresNumber) {
        SimpleFeatruresProcessor simpleFeatruresProcessor = new SimpleFeatruresProcessor().featuresNumber(featuresNumber);
        if (!options.quadratic()) {
            return simpleFeatruresProcessor;
        }
        return new QuadraticFeaturesProcessor()
                .featuresNumber(featuresNumber)
                .next(simpleFeatruresProcessor);
    }

    protected int ringBufferSize() {
        return options.ringSize() > 0 ? options.ringSize() : 2048;
    }
}
