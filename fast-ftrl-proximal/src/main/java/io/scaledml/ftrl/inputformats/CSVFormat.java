package io.scaledml.ftrl.inputformats;

import com.google.inject.Inject;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.featuresprocessors.FeatruresProcessor;
import io.scaledml.ftrl.util.LineBytesBuffer;
import io.scaledml.ftrl.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ilya Smagin ilya-sm@yandex-team.ru on 4/2/15.
 */
public class CSVFormat implements InputFormat {

    private static final Logger logger = LoggerFactory.getLogger(CSVFormat.class);

    private static final LineBytesBuffer NAMESPACE = new LineBytesBuffer("KU");

    private static final String CAT_PREFIX = "CAT";

    private FeatruresProcessor featruresProcessor;

    @Override
    public void parse(LineBytesBuffer line, SparseItem item) {
        item.clear();
        String[] splits = line.toString().split(",");


        try {
            double label = Double.parseDouble(splits[0]);
            item.label(Util.doublesEqual(1., label) ? 1. : 0.);

        } catch (NumberFormatException e) {
            logger.info("Unable to parse label for " + line.toString());
        }

        for (int i = 1; i < splits.length; i++) {
            LineBytesBuffer split = new LineBytesBuffer(CAT_PREFIX + i + splits[i]);
            featruresProcessor.addFeature(item, NAMESPACE, split, 1.);
        }
        featruresProcessor.finalize(item);
    }

    @Inject
    public CSVFormat featruresProcessor(FeatruresProcessor featruresProcessor) {
        this.featruresProcessor = featruresProcessor;
        return this;
    }
}
