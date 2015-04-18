package io.scaledml.ftrl.inputformats;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.options.ColumnsMask;
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

    private FeaturesProcessor featuresProcessor;
    private ColumnsMask columnsMask;

    @Override
    public void parse(LineBytesBuffer line, SparseItem item) {
        item.clear();
        String[] splits = line.toString().split(",");

        for (int colNum = 0; colNum < splits.length; colNum++) {
            String colValue = splits[colNum];
            ColumnsMask.ColumnType columnType = columnsMask.getCategory(colNum);
            switch (columnType) {
                case LABEL:
                    double label = Double.parseDouble(colValue);
                    item.label(Util.doublesEqual(1., label) ? 1. : 0.);
                    break;
                case ID:
                    break;
                case NUMERICAL:
                    if (!Strings.isNullOrEmpty(colValue)) {
                        LineBytesBuffer cat = new LineBytesBuffer(CAT_PREFIX + colNum);
                        double value = Double.parseDouble(colValue);
                        featuresProcessor.addFeature(item, NAMESPACE, cat, value);
                    }
                    break;
                case CATEGORICAL:
                    if (!Strings.isNullOrEmpty(colValue)) {
                        LineBytesBuffer catVaue = new LineBytesBuffer(CAT_PREFIX + colNum + colValue);
                        featuresProcessor.addFeature(item, NAMESPACE, catVaue, 1.);
                    }
                    break;
            }
        }

        featuresProcessor.finalize(item);
    }

    @Inject
    public CSVFormat featruresProcessor(FeaturesProcessor featuresProcessor) {
        this.featuresProcessor = featuresProcessor;
        return this;
    }

    @Inject
    CSVFormat csvMask(@Named("csvMask") ColumnsMask columnsMask) {
        this.columnsMask = columnsMask;
        return this;
    }
}
