package io.scaledml.ftrl.options;

import com.google.common.base.Joiner;
import com.lexicalscope.jewel.cli.Option;
import io.scaledml.core.inputformats.InputFormat;

import java.util.Arrays;

public interface FtrlOptions {
    @Option(longName = "feature_engineering",
            description = "Launch feature engineering program",
            hidden = true
    )
    boolean featureEngineering();
    @Option(shortName = "b", longName = "bit_precision", defaultValue = "18",
            maximum = 40, minimum = 1,
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
            description = "Final regressor to save (arg setInputStream filename)")
    String finalRegressor();

    @Option(shortName = "i", longName = "initial_regressor", defaultToNull = true,
            description = "Initial regressor(s) to load into memory (arg setInputStream filename)")
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
            description = "Show this help")
    boolean help();

    @Option(longName = "threads", defaultValue = "1",
            description = "Parallelization level")
    int threads();

    @Option(longName = "parallel-learn", hidden = true,
            description = "Make algorithm learn parallelLearn may be in cost of some quality loss. " +
                    "You should not use that property with threads < 8")
    boolean parallelLearn();

    @Option(longName = "format", defaultValue = "vw", description = "Input file format." +
            "vw, csv, binary are currently supported")
    InputFormatType format();

    @Option(longName = "custom-format-class", defaultToNull = true, description = "Input file format." +
            "vw, csv, binary are currently supported")
    String customInputFormatClass();

    @Option(longName = "ring_size", defaultValue = "2048", hidden = true)
    int ringSize();

    @Option(longName = "skip_first", description = "Skip first string of file(usually if it is a csv header", hidden = true)
    boolean skipFirst();

    @Option(longName = "csv_mask", description = "Csv columns information. It could contain " +
            "(l)Label, (i)d, (n)umeric or (c)categorical marks with amount of columns on the same type" +
            " in brackets[]." +
            "Some valid examples are: 'ilcccccnnnnn', 'lc[10]n",
            defaultValue = "lc")
    String csvMask();

    @Option(longName = "csv_delimiter",
            description ="csv columns delimiter. Must be only one character",
            defaultValue = ",")
    char csvDelimiter();
}
