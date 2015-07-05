package io.scaledml.ftrl;

import com.google.common.base.Throwables;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.scaledml.core.TwoPhaseEvent;
import io.scaledml.ftrl.featuresprocessors.FeaturesProcessor;
import io.scaledml.core.inputformats.*;
import io.scaledml.core.inputformats.ColumnsMask;
import io.scaledml.ftrl.options.FtrlOptions;
import io.scaledml.ftrl.options.InputFormatType;
import io.scaledml.ftrl.outputformats.FinishCollectStatisticsListener;
import io.scaledml.core.outputformats.NullOutputFormat;
import io.scaledml.core.outputformats.OutputFormat;
import io.scaledml.core.outputformats.PrintStreamOutputFormat;
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
import java.util.concurrent.Phaser;
import java.util.function.Supplier;


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
    Supplier<InputStream> inputStream() throws IOException {
        if (options.data() == null) {
            return () -> System.in;
        }
        return this::openIputFile;
    }

    private InputStream openIputFile() {
        try {
            return Files.newInputStream(Paths.get(options.data()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                options.ringSize(), threadsProvider,
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
        bindConstant().annotatedWith(Names.named("skipFirst")).to(options.skipFirst());
        try {
            bindInputFormat();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        bind(FeaturesProcessor.class);
        bind(FtrlProximalRunner.class).asEagerSingleton();
        bind(FinishCollectStatisticsListener.class).asEagerSingleton();
        bind(Phaser.class).asEagerSingleton();
    }

    private void bindInputFormat() throws ClassNotFoundException {
        if (options.customInputFormatClass() != null) {
            bind(InputFormat.class).to(Class.forName(
                    options.customInputFormatClass())
                        .asSubclass(InputFormat.class));
        } else {
            if (options.format() == InputFormatType.csv) {
                ColumnsMask columnsMask = new ColumnsMask(options.csvMask());
                bindConstant().annotatedWith(Names.named("csvDelimiter")).to(options.csvDelimiter());
                bind(new TypeLiteral<ColumnsMask>() {
                }).annotatedWith(Names.named("csvMask")).toInstance(columnsMask);
            }
            bind(InputFormat.class).to(options.format().formatClass);
        }
    }
}
