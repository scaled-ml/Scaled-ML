package io.scaledml.ftrl.inputformats;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.featuresprocessors.FeaturesProcessor;
import io.scaledml.ftrl.options.ColumnsMask;
import io.scaledml.ftrl.util.LineBytesBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ilya Smagin ilya-sm@yandex-team.ru on 4/2/15.
 */
public class CSVFormat implements InputFormat {

    private FeaturesProcessor featuresProcessor;
    private ColumnsMask columnsMask;
    private char csvDelimiter = ',';
    private final LineBytesBuffer valueBuffer = new LineBytesBuffer();
    private final LineBytesBuffer namespaceBuffer = new LineBytesBuffer();


    @Override
    public void parse(LineBytesBuffer line, SparseItem item) {
        item.clear();
        valueBuffer.clear();
        int colNum = 0;
        for (int i = 0; i < line.size(); i++) {
            byte b = line.get(i);
            if (((char) b) != csvDelimiter) {
                valueBuffer.append(b);
            } else {
                addFeature(item, colNum);
                colNum++;
                valueBuffer.clear();
            }
        }
        addFeature(item, colNum);
        featuresProcessor.finalize(item);
    }

    private void addFeature(SparseItem item, int colNum) {
        if (valueBuffer.empty()) {
            return;
        }
        namespaceBuffer.clear();
        namespaceBuffer.putInteger(colNum);
        ColumnsMask.ColumnType columnType = columnsMask.getCategory(colNum);
        switch (columnType) {
            case LABEL:
                double label = Double.parseDouble(valueBuffer.toAsciiString());
                item.label(label);
                break;
            case ID:
                break;
            case NUMERICAL:
                double value = Double.parseDouble(valueBuffer.toAsciiString());
                featuresProcessor.addFeature(item, namespaceBuffer, namespaceBuffer, value);
                break;
            case CATEGORICAL:
                featuresProcessor.addFeature(item, namespaceBuffer, valueBuffer, 1.);
                break;
        }
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

    @Inject
    CSVFormat csvDelimiter(@Named("csvDelimiter") char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
        return this;
    }
}
