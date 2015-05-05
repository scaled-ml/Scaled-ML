package integration;

import io.scaledml.ftrl.Main;
import io.scaledml.ftrl.options.FtrlOptionsObject;
import io.scaledml.ftrl.options.InputFormatType;
import org.junit.Test;

import java.io.IOException;

public class FeatureEngineeringITest extends BaseIntegrationTest {

    @Test
    public void testFeatureEngineering() throws IOException {
        Main.runFeatureEngineering(
                new FtrlOptionsObject()
                        .data(resourcePath("/ruslan-train-small.csv"))
                        .format(InputFormatType.csv)
                        .skipFirst(true)
                        .csvMask("lc[37]n")
                        .predictions(tempDirectory + "/input"));
    }
}
