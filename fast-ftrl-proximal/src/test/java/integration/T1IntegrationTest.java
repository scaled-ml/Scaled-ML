package integration;


import io.scaledml.ftrl.Main;
import io.scaledml.ftrl.options.FtrlOptionsObject;
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

public class T1IntegrationTest {
    private Path tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("ftrl-test");
    }

    @Test
    public void testRunFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .threads(3)
                .data(getClass().getResource("/train-small.vw").getPath()));
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .predictions(tempDirectory + "/predictions")
                .data(getClass().getResource("/test-small.vw").getPath()));
        assertEquals(0.47427705769071893, logLoss, 0.000000001);
        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().mapToDouble(Double::parseDouble).toArray();
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
                .data(getClass().getResource("/train-small.vw").getPath()));
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .threads(3)
                .scalable(true)
                .predictions(tempDirectory + "/predictions")
                .data(getClass().getResource("/test-small.vw").getPath()));
        assertEquals(0.4716154011659849, logLoss, 0.01);
        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().mapToDouble(Double::parseDouble).toArray();
        int predictionsNum = predictions.length;
        assertEquals(predictionsNum, 100);
        assertTrue(Arrays.stream(predictions).allMatch(p -> p < 0.5));
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }
}