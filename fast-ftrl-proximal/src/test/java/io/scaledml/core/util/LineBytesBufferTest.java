package io.scaledml.core.util;


import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            StringBuilder sb = new StringBuilder();
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
        try (FastBufferedInputStream stream = new FastBufferedInputStream(Files.newInputStream(fileExpected))) {
            try (BufferedWriter writer = Files.newBufferedWriter(fileActual, Charsets.US_ASCII)) {
                LineBytesBuffer buffer = new LineBytesBuffer();
                LineBytesBuffer line = new LineBytesBuffer();
                while (buffer.readLineFrom(stream)) {
                    buffer.drainTo(line);
                    writer.write(line.toAsciiString());
                    writer.newLine();
                }
            }
        }
        assertTrue(Files.readAllLines(fileExpected, Charsets.US_ASCII).equals(Files.readAllLines(fileActual, Charsets.US_ASCII)));
    }

    @Test
    public void testCompare() throws Exception {
        LineBytesBuffer b1 = new LineBytesBuffer();
        LineBytesBuffer b2 = new LineBytesBuffer();
        assertTrue(b1.compareTo(b2) == 0);
        b1.append((byte) 1);
        b1.append((byte) 1);
        b2.append((byte) 1);
        b2.append((byte) 2);
        assertTrue(b1.compareTo(b2) < 0);
        b1.clear();
        b2.clear();
        b1.append((byte) 1);
        b1.append((byte) 1);
        b2.append((byte) 1);
        b2.append((byte) 1);
        b2.append((byte) 1);
        assertTrue(b1.compareTo(b2) < 0);
    }

    @Test
    public void testPutReadShort() {
        LineBytesBuffer bb = new LineBytesBuffer();
        for (short num = -1024; num < 1024; num++) {
           assertEquals(2, bb.putShort(num));
        }
        bb.putShort(Short.MIN_VALUE);
        bb.putShort(Short.MAX_VALUE);
        AtomicInteger cursor = new AtomicInteger();
        for (short num = -1024; num < 1024; num++) {
            assertEquals(num, bb.readShort(cursor));
        }
        assertEquals(Short.MIN_VALUE, bb.readShort(cursor));
        assertEquals(Short.MAX_VALUE, bb.readShort(cursor));
    }

    @Test
    public void testPutReadString() {
        LineBytesBuffer bb = new LineBytesBuffer();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            sb.append(Integer.toBinaryString(i));
            sb.append(" oh ");
        }
        int writtenBytes = bb.putString(sb.toString());
        AtomicInteger cursor = new AtomicInteger(0);
        assertEquals(sb.toString(), bb.readString(cursor));
        assertEquals(writtenBytes, cursor.get());
    }

    @Test
    public void testPutReadLong() {
        LineBytesBuffer bb = new LineBytesBuffer();
        AtomicInteger cursor = new AtomicInteger(0);
        assertEquals(5, bb.putLong(0));
        assertEquals(0, bb.readLong(cursor));
        assertEquals(5, cursor.get());
        long bigNum = 7L * Integer.MAX_VALUE;
        assertTrue(bigNum > Integer.MAX_VALUE);
        for (long num = bigNum - 5000; num <= bigNum; num++) {
            assertEquals(5, bb.putLong(num));
        }
        for (long num = bigNum - 5000; num <= bigNum; num++) {
            assertEquals(num, bb.readLong(cursor));
        }
        bb.clear();
        cursor.set(0);
        long l1 = 338592878955L;
        assertTrue(bigNum < l1);
        assertEquals(5, bb.putLong(l1));
        assertEquals(l1, bb.readLong(cursor));
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }
}