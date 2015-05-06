package io.scaledml.core.util;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class LineBytesBuffer implements Comparable<LineBytesBuffer> {
    private static final byte MINUS_CODE = 0;
    private static final byte NL_CODE = 1;
    private static final byte RETURN_CODE = 2;
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
        while ((len = stream.readLine(bytes, start, bytes.length - start)) == bytes.length - start) {
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
        if (size >= bytes.length - 1) {
            bytes = ByteArrays.grow(bytes, bytes.length + 1024);
        }
        switch (b) {
            case -1:
                bytes[size++] = -1;
                bytes[size++] = MINUS_CODE;
                break;
            case (byte) '\n':
                bytes[size++] = -1;
                bytes[size++] = NL_CODE;
                break;
            case (byte) '\r':
                bytes[size++] = -1;
                bytes[size++] = RETURN_CODE;
                break;
            default:
                bytes[size++] = b;
        }

    }

    public void clear() {
        size = 0;
    }

    @Override
    public int compareTo(LineBytesBuffer o) {
        int minSize = Math.min(size, o.size);
        for (int i = 0; i < minSize; i++) {
            int byteCompare = Byte.compare(get(i), o.get(i));
            if (byteCompare != 0) {
                return byteCompare;
            }
        }
        return Integer.compare(size, o.size);
    }

    public boolean empty() {
        return size == 0;
    }

    public int putByte(byte b) {
        append(b);
        return 1;
    }

    public int putShort(short num) {
        append((byte) (num));
        append((byte) (num >> 8));
        return 2;
    }
    public int putString(String str) {
        byte[] strBytes = str.getBytes(Charsets.US_ASCII);
        assert strBytes.length < Short.MAX_VALUE;
        putShort((short) strBytes.length);
        bytes = ByteArrays.ensureCapacity(bytes, size + strBytes.length);
        System.arraycopy(strBytes, 0, bytes, size, strBytes.length);
        size += strBytes.length;
        return strBytes.length + 2;
    }

    public int putLong(long num) {
        assert num >= 0 && num < (1L << 40);
        append((byte) (num));
        append((byte) (num >> 8));
        append((byte) (num >> 16));
        append((byte) (num >> 24));
        append((byte) (num >> 32));
        return 5;
    }

    public int putInteger(int num) {
        append((byte) (num));
        append((byte) (num >> 8));
        append((byte) (num >> 16));
        append((byte) (num >> 24));
        return 4;
    }

    public int putFloat(float num) {
        return putInteger(Float.floatToIntBits(num));
    }

    public short readShort(AtomicInteger cursor) {
        return (short) (((readByte(cursor) & 0xff)) |
                 ((readByte(cursor) & 0xff) << 8));
    }

    public long readLong(AtomicInteger cursor) {
        return ((readByte(cursor) & 0xffL) |
                ((readByte(cursor) & 0xffL) << 8) |
                ((readByte(cursor) & 0xffL) << 16) |
                ((readByte(cursor) & 0xffL) << 24) |
                ((readByte(cursor) & 0xffL) << 32));
    }



    public String readString(AtomicInteger cursor) {
        short size = readShort(cursor);
        int start = cursor.get();
        return new String(bytes, start, cursor.addAndGet(size) - start, Charsets.US_ASCII);
    }

    public byte readByte(AtomicInteger cursor) {
        byte b = get(cursor.getAndIncrement());
        if (b != -1) {
            return b;
        }
        byte next = get(cursor.getAndIncrement());
        switch (next) {
            case MINUS_CODE:
                return -1;
            case NL_CODE:
                return '\n';
            case RETURN_CODE:
                return '\r';
            default:
                throw new IllegalStateException();
        }
    }


    public float readFloat(AtomicInteger cursor) {
        return Float.intBitsToFloat(readInt(cursor));
    }

    public int readInt(AtomicInteger cursor) {
        return ((readByte(cursor) & 0xff) |
                ((readByte(cursor) & 0xff) << 8) |
                ((readByte(cursor) & 0xff) << 16) |
                ((readByte(cursor)) << 24));
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
            if (get(i) != that.get(i)) {
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
