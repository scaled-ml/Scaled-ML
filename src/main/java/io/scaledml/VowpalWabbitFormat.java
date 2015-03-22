package io.scaledml;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.scaledml.io.LineBytesBuffer;

public class VowpalWabbitFormat {
    private static HashFunction murmur =  Hashing.murmur3_128(42);

    private final long featuresNumber;
    private final LineBytesBuffer buffer = new LineBytesBuffer();
    private final LineBytesBuffer namespace = new LineBytesBuffer();

    public VowpalWabbitFormat(long featuresNumber) {
        this.featuresNumber = featuresNumber;
    }

    private enum State {
        BEFORE_LABEL, LABEL, AFTER_LABEL, BEFORE_NAMESPACE, NAMESPACE, BEFORE_FEATURE, FEATURE;
    }

    private CharMatcher NUMBER_MATCHER = CharMatcher.DIGIT.or(CharMatcher.anyOf(".-")).precomputed();
    private CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("_")).precomputed();
    private CharMatcher PIPE_MATCHER = CharMatcher.anyOf("|").precomputed();

    public SparseItem parse(LineBytesBuffer line) {
        SparseItem item = new SparseItem();
        buffer.clear();
        namespace.clear();
        State state = State.BEFORE_LABEL;
        for (int i = 0; i < line.getLineLength(); i++) {
            byte b = line.get(i);
            char c = (char) b;
            switch (state) {
                case BEFORE_LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        buffer.append(b);
                        state = State.LABEL;
                    }
                    break;
                case LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        buffer.append(b);
                    } else {
                        item.setLabel(Double.parseDouble(buffer.toAsciiString()) > 0. ? 1. : 0.);
                        buffer.clear();
                        state = State.AFTER_LABEL;
                    }
                    break;
                case AFTER_LABEL:
                    if (PIPE_MATCHER.matches(c)) {
                        state = State.BEFORE_NAMESPACE;
                    }
                    break;
                case BEFORE_NAMESPACE:
                    if (NAME_MATCHER.matches(c)) {
                        buffer.append(b);
                        state = State.NAMESPACE;
                    }
                    break;
                case NAMESPACE:
                    if (NAME_MATCHER.matches(c)) {
                        buffer.append(b);
                    } else {
                        buffer.newLine();
                        buffer.drainLineTo(namespace);
                        assert buffer.getLineLength() == 0;
                        state = State.BEFORE_FEATURE;
                    }
                    break;
                case BEFORE_FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        buffer.append(b);
                        state = State.FEATURE;
                    } else if (PIPE_MATCHER.matches(c)) {
                        state = State.BEFORE_NAMESPACE;
                        namespace.clear();
                    }
                    break;
                case FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        buffer.append(b);
                    } else {
                        addIndex(item);
                        if (PIPE_MATCHER.matches(c)) {
                            state = State.BEFORE_NAMESPACE;
                            namespace.clear();
                        } else {
                            state = State.BEFORE_FEATURE;
                        }
                    }
                    break;
            }
        }
        if (state == State.FEATURE) {
            addIndex(item);
        }
        return item;
    }

    private void addIndex(SparseItem item) {
        item.addIndex(Math.abs(murmur.newHasher()
                        .putBytes(namespace.bytes(), 0, namespace.getLineLength())
                        .putBytes(buffer.bytes(), 0, buffer.getLineLength()).hash().asLong()) % featuresNumber);
        buffer.clear();
    }
}
