package io.scaledml.core.inputformats;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;

/**
 * @author Ilya Smagin ilya-sm@yandex-team.ru on 4/2/15.
 */
public class CSVFormat extends AbstractDelimiterSeparatedValuesFormat {

    private ColumnsMask columnsMask;
    private char csvDelimiter = ',';

    @Override
    protected void processColumn(SparseItem item, int colNum, LineBytesBuffer valueBuffer) {
        if (valueBuffer.empty()) {
            return;
        }
        namespaceBuffer.clear();
        namespaceBuffer.putShort((short) colNum);
        ColumnsMask.ColumnType columnType = columnsMask.getCategory(colNum);
        switch (columnType) {
            case LABEL:
                double label = Double.parseDouble(valueBuffer.toAsciiString());
                item.label(label);
                break;
            case ID:
                item.id(valueBuffer.toString());
                break;
            case NUMERICAL:
                double value = Double.parseDouble(valueBuffer.toAsciiString());
                featuresProcessor.addNumericalFeature(item, namespaceBuffer, namespaceBuffer, value);
                break;
            case CATEGORICAL:
                featuresProcessor.addCategoricalFeature(item, namespaceBuffer, valueBuffer);
                break;
        }
    }

    @Override
    protected char csvDelimiter() {
        return csvDelimiter;
    }

    @Inject
    public CSVFormat csvMask(@Named("csvMask") ColumnsMask columnsMask) {
        this.columnsMask = columnsMask;
        return this;
    }

    @Inject
    public CSVFormat csvDelimiter(@Named("csvDelimiter") char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
        return this;
    }
}
