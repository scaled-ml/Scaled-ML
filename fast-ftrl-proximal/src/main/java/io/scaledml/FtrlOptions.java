package io.scaledml;

import com.lexicalscope.jewel.cli.Option;

public interface FtrlOptions {

    @Option(shortName = "b", longName = "bit_precision", defaultValue = "18",
            maximum = 34, minimum = 1,
            description = "number of bits in the feature table")
    int hashcodeBits();

    @Option(longName = "ftrl_alpha", defaultValue = "0.005",
            description = "ftrl alpha parameter (option in ftrl)")
    double alfa();

    @Option(longName = "ftrl_beta", defaultValue = "0.1",
            description = "ftrl beta patameter (option in ftrl)")
    double beta();

    @Option(longName = "l1", defaultValue = "0.0",
            description = "l_1 lambda (L1 regularization)")
    double l1();

    @Option(longName = "l2", defaultValue = "0.0",
            description = "l_2 lambda (L2 regularization)")
    double l2();

    @Option(shortName = "f", longName = "final_regressor", defaultToNull = true,
            description = "Final regressor to save (arg inputStream filename)")
    String finalRegressor();

    @Option(shortName = "i", longName = "initial_regressor", defaultToNull = true,
            description = "Initial regressor(s) to load into memory (arg inputStream filename)")
    String initialRegressor();

    @Option(shortName = "t", longName = "testonly",
            description = "Ignore label information and just test")
    boolean testOnly();

    @Option(shortName = "d", longName = "data", defaultToNull = true,
            description = "Example Set")
    String data();

    @Option(shortName = "p", longName = "predictions", defaultToNull = true,
            description = "File to output predictions to")
    String predictions();

    @Option(shortName = "h", longName = "help", helpRequest = true,
            description = "show this help")
    boolean help();
}
