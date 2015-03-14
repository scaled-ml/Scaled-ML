package io.scaledml;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    interface FtrlOptions {

        @Option(shortName = "b", longName = "bit_precision", defaultValue = "18",
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

        @Option(shortName = "f", longName = "final_regressor",
                description = "Final regressor to save (arg is filename)")
        String finalRegressor();

        @Option(shortName = "i", longName = "initial_regressor", defaultToNull = true,
                description = "Initial regressor(s) to load into memory (arg is filename)")
        String initialRegressor();

        @Option(shortName = "t", longName = "testonly", defaultToNull = true,
                description = "Ignore label information and just test")
        boolean testOnly();

        @Option(shortName = "p", longName = "predictions", defaultToNull = true,
                description = "File to output predictions to")
        String predictions();

        @Option(shortName = "h", longName = "help", helpRequest = true,
                description = "show this help")
        boolean help();
    }

    public static void main(String ... args) throws Exception {
        FtrlOptions ftrlOptions = CliFactory.parseArguments(FtrlOptions.class, args);
        FTRLProximal model;
        if (ftrlOptions.initialRegressor() != null) {
            try (ObjectInputStream is = new ObjectInputStream(
                    Files.newInputStream(Paths.get(ftrlOptions.initialRegressor())))) {
                model = (FTRLProximal) is.readObject();
            }
        } else {
            model = new FTRLProximal(ftrlOptions.hashcodeBits(),
                    ftrlOptions.l1(), ftrlOptions.l2(),
                    ftrlOptions.alfa(), ftrlOptions.beta()
                    );
        }
        VowpalWabbitFormat format = new VowpalWabbitFormat(model.featuresNum());
        if (!ftrlOptions.testOnly()) {
            applyModel(format, ftrlOptions.predictions(), model::train);
        } else {
            applyModel(format, ftrlOptions.predictions(), model::test);
        }
        if (ftrlOptions.finalRegressor() != null) {
            try (ObjectOutputStream os  = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(ftrlOptions.finalRegressor())))) {
                os.writeObject(model);
            }
        }
    }

    interface ItemProcessor {
        double apply(SparseItem item);
    }

    interface PredictionConsumer {
       void consume(String line) throws IOException;
    }

    private static void consume(String line) throws IOException {

    }

    private static void applyModel(VowpalWabbitFormat format, PredictionConsumer predictionsConsumer, ItemProcessor seeingItem) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            SparseItem item = format.parse(scanner.nextLine());
            predictionsConsumer.consume(Double.toString(seeingItem.apply(item)) + "\n");
        }
        System.out.println("Done");
    }

    private static void applyModel(VowpalWabbitFormat format, String predictionsFile, ItemProcessor seeingItem) throws IOException {
        if (predictionsFile == null) {
            applyModel(format, Main::consume, seeingItem);
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(predictionsFile))) {
                applyModel(format, writer::write, seeingItem);
            }
        }
    }
}