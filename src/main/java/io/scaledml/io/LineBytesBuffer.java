package io.scaledml.io;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;

public class LineBytesBuffer {
    private static final byte END_LINE_BYTE = (byte) '\n';
    private byte[] bytes;
    private int lineLength;
    private int readBytes;

    public LineBytesBuffer() {
        bytes = new byte[1024];
        lineLength = 0;
    }

    public void drainLineTo(LineBytesBuffer other) {
        assert readBytes > lineLength;
        byte[] tmp = other.bytes;
        other.bytes = bytes;
        other.lineLength = lineLength;
        bytes = tmp;
        int newSize = readBytes - 1 - lineLength;
        bytes = ByteArrays.ensureCapacity(bytes, newSize);
        System.arraycopy(other.bytes, lineLength + 1, bytes, 0, newSize);
        lineLength = newSize;
        readBytes = newSize;
    }

    public int getLineLength() {
        return lineLength;
    }

    public byte get(int i) {
        return bytes[i];
    }

    public boolean readLineFrom(InputStream stream) throws IOException {
        assert readBytes == lineLength;
        for (int i = 0; i < readBytes; i++) {
            if (bytes[i] == END_LINE_BYTE) {
                lineLength = i;
                return true;
            }
        }
        if (readBytes == bytes.length) {
            bytes = ByteArrays.grow(bytes, bytes.length * 3 / 2);
        }
        int read = stream.read(bytes, readBytes, bytes.length - readBytes);
        while (read >= 0) {
            readBytes += read;
            for (int i = 0; i < read; i++) {
                if (bytes[lineLength] == END_LINE_BYTE) {
                    return true;
                }
                lineLength++;
            }
            if (readBytes == bytes.length) {
                bytes = ByteArrays.grow(bytes, bytes.length * 3 / 2);
            }
            read = stream.read(bytes, readBytes, bytes.length - readBytes);
        }
        assert readBytes == lineLength;
        if (lineLength > 0) {
            readBytes++;
            return true;
        }
        return false;
    }

    public String toAsciiString() {
        return new String(bytes, 0, lineLength, Charsets.US_ASCII);
    }

    @Override
    public String toString() {
        return toAsciiString();
    }

    public void newLine() {
        readBytes++;
    }

    public byte[] bytes() {
        return bytes;
    }

    public void append(byte b) {
        assert readBytes == lineLength;
        if (readBytes == bytes.length) {
            bytes = ByteArrays.grow(bytes, bytes.length * 3 / 2);
        }
        bytes[readBytes] = b;
        readBytes++;
        lineLength++;
    }

    public void clear() {
        lineLength = 0;
        readBytes = 0;
    }
}
