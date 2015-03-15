package io.scaledml;


import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.DoubleSummaryStatistics;
import java.util.function.Consumer;

public class MainTest {
    private Path tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("ftrl-test");
    }

    @Test
    public void testRunFtrlProximal() throws Exception {
        Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor(tempDirectory + "/model")
                .data(getClass().getResource("/train-small.vw").getPath()));
        Main.runFtrlProximal(new FtrlOptionsObject()
                .initialRegressor(tempDirectory + "/model")
                .testOnly(true)
                .predictions(tempDirectory + "/predictions")
                .data(getClass().getResource("/test-small.vw").getPath()));
        long predictionsNum = Files.readAllLines(Paths.get(tempDirectory.toString(), "predictions"))
                .stream().mapToDouble(Double::parseDouble).count();
        assertEquals(predictionsNum, 100L);
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    // TODO: move to lambda-plus
    @FunctionalInterface
    public interface ConsumerWithException<T, E extends Exception> {
        void apply(T el) throws E;
    }

    public static <T, E extends Exception> Consumer<T> wrapUnchecked(
            ConsumerWithException<T, E> consumer) {
        return element -> {
            try {
                consumer.apply(element);
            } catch (Exception exception) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException(exception);
            }
        };
    }
}