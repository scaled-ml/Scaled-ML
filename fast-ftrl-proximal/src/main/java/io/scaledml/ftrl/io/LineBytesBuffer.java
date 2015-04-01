package io.scaledml.ftrl.io;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;

public class LineBytesBuffer {
    private byte[] bytes;
    private int size;

    public LineBytesBuffer() {
        bytes = new byte[1024];
        size = 0;
    }

    public void drainTo(LineBytesBuffer other) {
        byte[] tmp = other.bytes;
        other.bytes = bytes;
        other.size = size;
        bytes = tmp;
        size = 0;
    }

    public int size() {
        return size;
    }

    public byte get(int i) {
        return bytes[i];
    }

    public boolean readLineFrom(FastBufferedInputStream stream) throws IOException {
        int start = 0, len;
        while((len = stream.readLine(bytes, start, bytes.length - start)) == bytes.length - start ) {
            start += len;
            bytes = ByteArrays.grow(bytes, bytes.length + 1024);
        }
        size = start + Math.max(len, 0);
        return !(size == 0 && len < 0);
    }

    public String toAsciiString() {
        return new String(bytes, 0, size, Charsets.US_ASCII);
    }

    @Override
    public String toString() {
        return toAsciiString();
    }

    public byte[] bytes() {
        return bytes;
    }

    public void append(byte b) {
        if (size == bytes.length) {
            bytes = ByteArrays.grow(bytes, bytes.length + 1024);
        }
        bytes[size] = b;
        size++;
    }

    public void putInteger(int num) {
        append(int0(num));
        append(int1(num));
        append(int2(num));
        append(int3(num));
    }

    private static byte int3(int x) { return (byte)(x >> 24); }
    private static byte int2(int x) { return (byte)(x >> 16); }
    private static byte int1(int x) { return (byte)(x >>  8); }
    private static byte int0(int x) { return (byte)(x      ); }

    public void clear() {
        size = 0;
    }
}
