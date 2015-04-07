package io.scaledml.ftrl.util;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.util.Arrays;

public class LineBytesBuffer implements Comparable<LineBytesBuffer> {
    private byte[] bytes;
    private int size;

    public LineBytesBuffer() {
        bytes = new byte[1024];
        size = 0;
    }

    public LineBytesBuffer(String str) {
        this.bytes = str.getBytes(Charsets.UTF_8);
        size = bytes.length;
    }

    public LineBytesBuffer(LineBytesBuffer other) {
        this.bytes = Arrays.copyOf(other.bytes, other.size);
        size = other.size;
    }

    public void drainTo(LineBytesBuffer other) {
        byte[] tmp = other.bytes;
        other.bytes = bytes;
        other.size = size;
        bytes = tmp;
        size = 0;
    }

    public void setContentOf(LineBytesBuffer feature) {
        size = feature.size;
        bytes = ByteArrays.ensureCapacity(bytes, size);
        System.arraycopy(feature.bytes, 0, bytes, 0, size);
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
        append((byte) (num));
        append((byte) (num >> 8));
        append((byte) (num >> 16));
        append((byte) (num >> 24));
    }

    public void clear() {
        size = 0;
    }

    @Override
    public int compareTo(LineBytesBuffer o) {
        int minSize = Math.min(size, o.size);
        for (int i = 0; i < minSize; i++) {
            int byteCompare = Byte.compare(bytes[i], o.bytes[i]);
            if (byteCompare != 0) {
                return byteCompare;
            }
        }
        return Integer.compare(size, o.size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LineBytesBuffer that = (LineBytesBuffer) o;
        if (size != that.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (bytes[i] != that.bytes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Util.murmur32().hashBytes(bytes, 0, size).hashCode();
    }
}
