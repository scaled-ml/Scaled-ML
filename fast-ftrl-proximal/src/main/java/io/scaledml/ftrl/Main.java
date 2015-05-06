package io.scaledml.ftrl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import io.scaledml.features.FeatureEngineeringModule;
import io.scaledml.features.FeatureEngineeringRunner;
import io.scaledml.ftrl.options.FtrlOptions;
import io.scaledml.ftrl.outputformats.FinishCollectStatisticsListener;
import io.scaledml.ftrl.parallel.ParallelModule;
import io.scaledml.ftrl.semiparallel.SemiParallelModule;

import java.io.IOException;

public class Main {

    public static void main(String... args) throws Exception {
        FtrlOptions ftrlOptions;
        try {
            ftrlOptions = CliFactory.parseArguments(FtrlOptions.class, args);
        } catch (ArgumentValidationException e) {
            System.out.println(e.getMessage());
            return;
        }
        if (ftrlOptions.featureEngineering()) {
            runFeatureEngineering(ftrlOptions);
        } else {
            runFtrlProximal(ftrlOptions);
        }
    }

    public static void runFeatureEngineering(FtrlOptions ftrlOptions) throws IOException {
        Injector injector = Guice.createInjector(new FeatureEngineeringModule(ftrlOptions));
        FeatureEngineeringRunner runner = injector.getInstance(FeatureEngineeringRunner.class);
        runner.process();
    }

    public static double runFtrlProximal(FtrlOptions ftrlOptions) throws Exception {
        Injector injector = createInjector(ftrlOptions);
        FtrlProximalRunner runner = injector.getInstance(FtrlProximalRunner.class);
        runner.process();
        return injector.getInstance(FinishCollectStatisticsListener.class).logLoss();
    }

    private static Injector createInjector(FtrlOptions ftrlOptions) {
        if (ftrlOptions.parallelLearn()) {
            return Guice.createInjector(new ParallelModule(ftrlOptions));
        }
        return Guice.createInjector(new SemiParallelModule(ftrlOptions));
    }
}