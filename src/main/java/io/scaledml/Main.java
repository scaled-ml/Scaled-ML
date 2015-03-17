package io.scaledml;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;

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
        applyModel(format, model, ftrlOptions.testOnly(), ftrlOptions.data(), ftrlOptions.predictions());

        if (ftrlOptions.finalRegressor() != null) {
            try (ObjectOutputStream os  = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(ftrlOptions.finalRegressor())))) {
                os.writeObject(model);
            }
        }
    }

    private static void applyModel(VowpalWabbitFormat format, FTRLProximal model, boolean testOnly, String data, String predictions) throws IOException {
        InputStream is;
        if (data == null) {
            is = System.in;
        } else {
            is = Files.newInputStream(Paths.get(data));
        }
        ItemProcessor processor;
        if (testOnly) {
            processor = model::test;
        } else {
            processor = model::train;
        }
        if (predictions != null && !predictions.equals("/dev/stdout")) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(predictions))) {
                applyModel(format, processor, is, writer::write);
                return;
            }
        }
        PredictionConsumer consumer;
        if (predictions == null) {
            consumer = Main::consume;
        } else {
            consumer = System.out::print;
        }
        applyModel(format, processor, is, consumer);
    }

    static class Statistics {
        double lossSum = 0.;
        long itemNo = 0;
        long nextItemNoToPrint = 1;

        void consume(SparseItem item, double prediction) {
            itemNo++;
            lossSum += Math.abs(item.getLabel() - prediction);
            if (itemNo == nextItemNoToPrint) {
                nextItemNoToPrint *= 2;
                System.out.println(lossSum / itemNo + "\t" + itemNo + "\t" + item.getLabel() + "");
            }

        }
    }
    private static void applyModel(VowpalWabbitFormat format, ItemProcessor processor, InputStream is, PredictionConsumer consumer)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SparseItem item = format.parse(line);
                double prediction = processor.apply(item);

                consumer.consume(prediction + "\n");
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
}