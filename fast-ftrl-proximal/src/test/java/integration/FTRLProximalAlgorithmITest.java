package integration;


import io.scaledml.core.util.Util;
import io.scaledml.ftrl.Main;
import io.scaledml.ftrl.options.FtrlOptionsObject;
import io.scaledml.ftrl.options.InputFormatType;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FTRLProximalAlgorithmITest extends BaseIntegrationTest {

    @Test
    public void testRunWvFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .threads(3)
                .data(resourcePath("/train-small.vw")));
        syncFS();
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .predictions(tempDirectory + "/predictions")
                .data(resourcePath("/test-small.vw")));
        syncFS();
        assertEquals(0.47427705769071893, logLoss, Util.EPSILON);
        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().map(s -> s.split("\t")[1]).mapToDouble(Double::parseDouble).toArray();
        int predictionsNum = predictions.length;
        assertEquals(predictionsNum, 100);

        assertTrue(Arrays.stream(predictions).allMatch(p -> p < 0.5));
        assertEquals(0.2355821069092084, predictions[0], 0.001);
        assertEquals(0.2495902538274775, predictions[63], 0.001);
    }

    @Test
    public void testRunParallelFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .threads(3)
                .scalable(true)
                .data(resourcePath("/train-small.vw")));
        syncFS();
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .threads(3)
                .scalable(true)
                .predictions(tempDirectory + "/predictions")
                .data(resourcePath("/test-small.vw")));
        syncFS();
        assertEquals(0.4716154011659849, logLoss, 0.01);
        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().map(s -> s.split("\t")[1]).mapToDouble(Double::parseDouble).toArray();
        int predictionsNum = predictions.length;
        assertEquals(predictionsNum, 100);
        assertTrue(Arrays.stream(predictions).allMatch(p -> p < 0.5));
    }

    @Test
    public void testRunCsvFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .threads(3)
                .data(resourcePath("/ruslan-train-small.csv"))
                .format(InputFormatType.csv)
                .skipFirst(true));
        syncFS();
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .predictions(tempDirectory + "/predictions")
                .data(resourcePath("/ruslan-test-small.csv"))
                .format(InputFormatType.csv)
                .skipFirst(true));
        syncFS();
        assertEquals(0.5183230180345785, logLoss, Util.EPSILON);

        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().map(s -> s.split("\t")[1]).mapToDouble(Double::parseDouble).toArray();
        int predictionsNum = predictions.length;
        assertEquals(predictionsNum, 100);
    }
}