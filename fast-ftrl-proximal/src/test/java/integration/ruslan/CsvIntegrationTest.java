package integration.ruslan;


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

import static org.junit.Assert.assertEquals;

public class CsvIntegrationTest {
    private Path tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("csv-ftrl-test");
    }

    @Test
    public void testRunFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .threads(3)
                .data(getClass().getResource("/ruslan-train-small.csv").getPath())
                .format("csv")
                .skipFirst(true)
                .csvMask("lc[37]n"));

        Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .predictions(tempDirectory + "/predictions")
                .data(getClass().getResource("/ruslan-test-small.csv").getPath())
                .format("csv")
                .skipFirst(true)
                .csvMask("lc[37]n"));

        double[] predictions = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().mapToDouble(Double::parseDouble).toArray();
        int predictionsNum = predictions.length;
        assertEquals(predictionsNum, 100);
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }
}