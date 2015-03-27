package io.scaledml;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import io.scaledml.io.LineBytesBuffer;
import io.scaledml.semiparallel.SemiParallelModule;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String ... args) throws Exception {
        FtrlOptions ftrlOptions;
        try {
            ftrlOptions = CliFactory.parseArguments(FtrlOptions.class, args);
        } catch (ArgumentValidationException e) {
            System.out.println(e.getMessage());
            return;
        }
        runFtrlProximal(ftrlOptions);
    }

    public static void runFtrlProximal(FtrlOptions ftrlOptions) throws Exception {
        Injector injector = Guice.createInjector(new SemiParallelModule(ftrlOptions));
        FtrlProximalRunner runner = injector.getInstance(FtrlProximalRunner.class);
        runner.process();
    }
}