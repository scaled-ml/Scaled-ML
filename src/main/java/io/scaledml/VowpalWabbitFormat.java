package io.scaledml;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class VowpalWabbitFormat {
    private static HashFunction murmur =  Hashing.murmur3_128(42);

    private final long featuresNumber;

    public VowpalWabbitFormat(long featuresNumber) {
        this.featuresNumber = featuresNumber;
    }

    private enum State {
        BEFORE_LABEL, LABEL, AFTER_LABEL, BEFORE_NAMESPACE, NAMESPACE, BEFORE_FEATURE, FEATURE;
    }

    private CharMatcher NUMBER_MATCHER = CharMatcher.DIGIT.or(CharMatcher.anyOf(".-")).precomputed();
    private CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("_")).precomputed();
    private CharMatcher PIPE_MATCHER = CharMatcher.anyOf("|").precomputed();

    public SparseItem parse(String line) {
        SparseItem item = new SparseItem();
        StringBuilder sb = new StringBuilder();
        String namespace = null;
        State state = State.BEFORE_LABEL;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (state) {
                case BEFORE_LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        sb.append(c);
                        state = State.LABEL;
                    }
                    break;
                case LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        sb.append(c);
                    } else {
                        item.setLabel(Double.parseDouble(sb.toString()) > 0. ? 1. : 0.);
                        sb.setLength(0);
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
                        sb.append(c);
                        state = State.NAMESPACE;
                    }
                    break;
                case NAMESPACE:
                    if (NAME_MATCHER.matches(c)) {
                        sb.append(c);
                    } else {
                        namespace = sb.toString();
                        sb.setLength(0);
                        state = State.BEFORE_FEATURE;
                    }
                    break;
                case BEFORE_FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        sb.append(c);
                        state = State.FEATURE;
                    } else if (PIPE_MATCHER.matches(c)) {
                        state = State.BEFORE_NAMESPACE;
                        namespace = null;
                    }
                    break;
                case FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        sb.append(c);
                    } else {
                        item.addIndex(Math.abs(murmur.newHasher()
                                        .putString(namespace, Charsets.US_ASCII)
                                       .putString(sb, Charsets.US_ASCII).hash().asLong()) % featuresNumber);
                        sb.setLength(0);
                        if (PIPE_MATCHER.matches(c)) {
                            state = State.BEFORE_NAMESPACE;
                            namespace = null;
                        } else {
                            state = State.BEFORE_FEATURE;
                        }
                    }
                    break;
            }
        }
        if (state == State.FEATURE) {
            item.addIndex(Math.abs(murmur.newHasher()
                    .putString(namespace, Charsets.US_ASCII)
                    .putString(sb, Charsets.US_ASCII).hash().asLong()) % featuresNumber);
        }
        return item;
    }
}
