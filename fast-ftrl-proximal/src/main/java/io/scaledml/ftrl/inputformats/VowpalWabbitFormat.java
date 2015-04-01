package io.scaledml.ftrl.inputformats;

import com.google.common.base.CharMatcher;
import com.google.inject.Inject;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.Util;
import io.scaledml.ftrl.io.LineBytesBuffer;

public class VowpalWabbitFormat implements InputFormat {
    private static final char NAME_CHAR = 'z';
    private final LineBytesBuffer buffer = new LineBytesBuffer();
    private final LineBytesBuffer namespace = new LineBytesBuffer();
    private FeatruresProcessor featruresProcessor;

    private enum State {
        BEFORE_LABEL, LABEL, AFTER_LABEL, BEFORE_NAMESPACE, NAMESPACE, BEFORE_FEATURE, FEATURE
    }

    private final CharMatcher NUMBER_MATCHER = CharMatcher.DIGIT.or(CharMatcher.anyOf(".-")).precomputed();
    private final CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("_")).precomputed();
    private final CharMatcher PIPE_MATCHER = CharMatcher.anyOf("|").precomputed();

    @Override
    public void parse(LineBytesBuffer line, SparseItem item) {
        item.clear();
        buffer.clear();
        namespace.clear();
        State state = State.BEFORE_LABEL;
        for (int i = 0; i < line.size(); i++) {
            byte b = line.get(i);
            char c = b > 0 ? (char) b : NAME_CHAR;
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
                        item.label(Util.doublesEqual(1., Double.parseDouble(buffer.toAsciiString())) ? 1. : 0.);
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
                        buffer.drainTo(namespace);
                        assert buffer.size() == 0;
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
        featruresProcessor.finalize(item);
    }

    private void addIndex(SparseItem item) {
        featruresProcessor.addFeature(item, namespace, buffer, 1.);
        buffer.clear();
    }

    @Inject
    public VowpalWabbitFormat featruresProcessor(FeatruresProcessor featruresProcessor) {
        this.featruresProcessor = featruresProcessor;
        return this;
    }
}
