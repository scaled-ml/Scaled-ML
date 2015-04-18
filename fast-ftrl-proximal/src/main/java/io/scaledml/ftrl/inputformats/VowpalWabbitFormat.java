package io.scaledml.ftrl.inputformats;

import com.google.common.base.CharMatcher;
import com.google.inject.Inject;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.featuresprocessors.FeaturesProcessor;
import io.scaledml.ftrl.util.LineBytesBuffer;
import io.scaledml.ftrl.util.Util;

public class VowpalWabbitFormat implements InputFormat {
    private static final char NAME_CHAR = 'z';
    private final LineBytesBuffer feature = new LineBytesBuffer();
    private final LineBytesBuffer namespace = new LineBytesBuffer();
    private final LineBytesBuffer number = new LineBytesBuffer();

    private FeaturesProcessor featuresProcessor;

    private enum State {
        BEFORE_LABEL, LABEL, AFTER_LABEL, BEFORE_NAMESPACE, NAMESPACE, BEFORE_FEATURE, FEATURE, FEATURE_VALUE
    }

    private final CharMatcher NUMBER_MATCHER = CharMatcher.DIGIT.or(CharMatcher.anyOf(".-")).precomputed();
    private final CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("_=-")).precomputed();
    private final CharMatcher PIPE_MATCHER = CharMatcher.anyOf("|").precomputed();
    private final CharMatcher COLON_MATCHER = CharMatcher.anyOf(":").precomputed();


    @Override
    public void parse(LineBytesBuffer line, SparseItem item) {
        item.clear();
        feature.clear();
        namespace.clear();
        number.clear();
        State state = State.BEFORE_LABEL;
        for (int i = 0; i < line.size(); i++) {
            byte b = line.get(i);
            char c = b > 0 ? (char) b : NAME_CHAR;
            switch (state) {
                case BEFORE_LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        number.append(b);
                        state = State.LABEL;
                    }
                    break;
                case LABEL:
                    if (NUMBER_MATCHER.matches(c)) {
                        number.append(b);
                    } else {
                        item.label(Util.doublesEqual(1., Double.parseDouble(number.toAsciiString())) ? 1. : 0.);
                        number.clear();
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
                        namespace.append(b);
                        state = State.NAMESPACE;
                    }
                    break;
                case NAMESPACE:
                    if (NAME_MATCHER.matches(c)) {
                        namespace.append(b);
                    } else {
                        state = State.BEFORE_FEATURE;
                    }
                    break;
                case BEFORE_FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        feature.append(b);
                        state = State.FEATURE;
                    } else if (PIPE_MATCHER.matches(c)) {
                        state = State.BEFORE_NAMESPACE;
                        namespace.clear();
                    }
                    break;
                case FEATURE:
                    if (NAME_MATCHER.matches(c)) {
                        feature.append(b);
                    } else if (PIPE_MATCHER.matches(c)) {
                        addIndex(item, 1.);
                        state = State.BEFORE_NAMESPACE;
                        namespace.clear();
                    } else if (COLON_MATCHER.matches(c)) {
                        state = State.FEATURE_VALUE;
                    } else {
                        addIndex(item, 1.);
                        state = State.BEFORE_FEATURE;
                    }
                    break;
                case FEATURE_VALUE:
                    if (NUMBER_MATCHER.matches(c)) {
                        number.append(b);
                    } else if (PIPE_MATCHER.matches(c)) {
                        addIndex(item, Double.parseDouble(number.toAsciiString()));
                        state = State.BEFORE_NAMESPACE;
                        namespace.clear();
                    } else {
                        addIndex(item, Double.parseDouble(number.toAsciiString()));
                        state = State.BEFORE_FEATURE;
                    }
            }
        }
        if (state == State.FEATURE) {
            addIndex(item, 1.);
        }
        if (state == State.FEATURE_VALUE) {
            addIndex(item, Double.parseDouble(number.toAsciiString()));
        }
        featuresProcessor.finalize(item);
    }

    private void addIndex(SparseItem item, double value) {
        featuresProcessor.addFeature(item, namespace, feature, value);
        feature.clear();
        number.clear();
    }

    @Inject
    public VowpalWabbitFormat featruresProcessor(FeaturesProcessor featuresProcessor) {
        this.featuresProcessor = featuresProcessor;
        return this;
    }
}
