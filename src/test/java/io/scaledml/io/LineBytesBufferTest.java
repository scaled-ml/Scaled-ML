package io.scaledml.io;


import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LineBytesBufferTest {
    private Path tempDirectory;

    @Before
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("ftrl-test");
    }

    @Test
    public void testReadLine() throws Exception {
        Path fileExpected = Paths.get(tempDirectory.toString(), "expected");
        try (BufferedWriter writer = Files.newBufferedWriter(fileExpected, Charsets.US_ASCII)) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 2000; i++) {
                for (int j = 0; j < i; j++) {
                    sb.append(j);
                    sb.append(' ');
                }
                sb.append('\n');
            }
            for (int i = 2000; i > 0; i--) {
                for (int j = 0; j < i; j++) {
                    sb.append(j);
                    sb.append(' ');
                }
                sb.append('\n');
                writer.write(sb.toString());
                sb.setLength(0);
            }
        }
        Path fileActual = Paths.get(tempDirectory.toString(), "actual");
        try (InputStream stream = Files.newInputStream(fileExpected)) {
            try (BufferedWriter writer = Files.newBufferedWriter(fileActual, Charsets.US_ASCII)) {
                LineBytesBuffer buffer = new LineBytesBuffer();
                LineBytesBuffer line = new LineBytesBuffer();
                while (buffer.readLineFrom(stream)) {
                    buffer.drainLineTo(line);
                    writer.write(line.toAsciiString());
                    writer.newLine();
                }
            }
        }
        assertEquals(Files.readAllLines(fileExpected, Charsets.US_ASCII), Files.readAllLines(fileActual, Charsets.US_ASCII));
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }
}