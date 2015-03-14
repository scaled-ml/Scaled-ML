package io.scaledml;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VowpalWabbitFormat {

    private final static Pattern VW_ITEM_PATTERN = Pattern.compile("^(-?[0-9\\.]+)(( +\\| *[^\\| ]+( +[^\\| ]+)+)+)$");
    private final static Pattern NAMESPACE_PATTERN = Pattern.compile("(\\| *[^\\| ]+)(( +[^\\| ]+)+)");
    private final static Pattern FEATURE_PATTERN = Pattern.compile("( +[^\\| ]+)");
    private static HashFunction murmur =  Hashing.murmur3_128(42);

    private final long featuresNumber;

    public VowpalWabbitFormat(long featuresNumber) {
        this.featuresNumber = featuresNumber;
    }

    public SparseItem parse(String line) {
        Matcher matcher = VW_ITEM_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        SparseItem item = new SparseItem();
        float label = Float.parseFloat(matcher.group(1).trim());
        item.setLabel(label);
        String labels = matcher.group(2);
        Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(labels);
        while (namespaceMatcher.find()) {
            String nameSpace = namespaceMatcher.group(1);
            String features = namespaceMatcher.group(2);
            Matcher featuresMatcher = FEATURE_PATTERN.matcher(features);
            while (featuresMatcher.find()) {
                String feature = featuresMatcher.group();
                long hashCode = murmur.newHasher()
                        .putUnencodedChars(nameSpace)
                        .putUnencodedChars(feature).hash().asLong() % featuresNumber;
                item.addIndex(hashCode);
            }
        }
        return item;
    }
}
