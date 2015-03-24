package io.scaledml;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import io.scaledml.io.LineBytesBuffer;
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
        FTRLProximalAlgorithm model;
        if (ftrlOptions.initialRegressor() != null) {
            try (ObjectInputStream is = new ObjectInputStream(
                    Files.newInputStream(Paths.get(ftrlOptions.initialRegressor())))) {
                model = (FTRLProximalAlgorithm) is.readObject();
            }
        } else {
            model = new FTRLProximalAlgorithm(ftrlOptions.hashcodeBits(),
                    ftrlOptions.l1(), ftrlOptions.l2(),
                    ftrlOptions.alfa(), ftrlOptions.beta()
                    );
        }
        InputFormat format = new VowpalWabbitFormat(model.featuresNum());
        applyModel(format, model, ftrlOptions.testOnly(), ftrlOptions.data(), ftrlOptions.predictions());

        if (ftrlOptions.finalRegressor() != null) {
            try (ObjectOutputStream os  = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(ftrlOptions.finalRegressor())))) {
                os.writeObject(model);
            }
        }
    }

    private static void applyModel(InputFormat format, FTRLProximalAlgorithm model, boolean testOnly, String data,
                                   String predictions) throws IOException {
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
        double logLikelyhood = 0.;
        long itemNo = 0;
        long nextItemNoToPrint = 1;

        void consume(SparseItem item, double prediction) {
            itemNo++;
            logLikelyhood += Math.log(item.getLabel() - 0. > 0.9 ?  prediction : 1 - prediction);
            if (itemNo == nextItemNoToPrint) {
                nextItemNoToPrint *= 2;
                System.out.println(-logLikelyhood / itemNo + "\t" + itemNo + "\t" +
                                item.getLabel() + "\t" + prediction + "\t" +
                                item.getIndexes().size());
            }

        }
    }

    private static void applyModel(InputFormat format, ItemProcessor processor, InputStream is,
                                   PredictionConsumer consumer)
            throws IOException {
        try (FastBufferedInputStream stream = new FastBufferedInputStream(is)) {
            Statistics outputStatistics = new Statistics();
            LineBytesBuffer readBuffer = new LineBytesBuffer();
            while (readBuffer.readLineFrom(stream)) {
                SparseItem item = format.parse(readBuffer);
                readBuffer.clear();
                double prediction = processor.apply(item);
                outputStatistics.consume(item, prediction);
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