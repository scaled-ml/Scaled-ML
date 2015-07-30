package integration;

import io.scaledml.core.util.Util;
import io.scaledml.ftrl.Main;
import io.scaledml.ftrl.options.FtrlOptionsObject;
import io.scaledml.ftrl.options.InputFormatType;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Ignore
public class FeatureEngineeringITest extends BaseIntegrationTest {

    @Test
    public void testFeatureEngineering() throws Exception {
        Main.runFeatureEngineering(
                new FtrlOptionsObject()
                        .data(resourcePath("/ruslan-train-small.csv"))
                        .format(InputFormatType.csv)
                        .skipFirst(true)
                        .csvMask("lc[37]n")
                        .predictions(tempDirectory + "/input"));
        syncFS();
        double logLoss = Main.runFtrlProximal(new FtrlOptionsObject()
                .threads(3)
                .data(tempDirectory + "/input")
                .format(InputFormatType.binary)
                .predictions(tempDirectory + "/output"));
        syncFS();
        assertEquals(0.5125, logLoss, 0.01);
        assertEquals(1000, Files.readAllLines(Paths.get(tempDirectory + "/output")).size());
    }
}
