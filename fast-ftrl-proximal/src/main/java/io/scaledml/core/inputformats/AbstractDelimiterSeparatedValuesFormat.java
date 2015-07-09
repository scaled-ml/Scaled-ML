package io.scaledml.core.inputformats;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.core.SparseItem;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.ftrl.featuresprocessors.FeaturesProcessor;

/**
 * Created by artem on 7/8/15.
 */
public abstract class AbstractDelimiterSeparatedValuesFormat implements InputFormat {
    private final LineBytesBuffer valueBuffer = new LineBytesBuffer();
    protected final LineBytesBuffer namespaceBuffer = new LineBytesBuffer();
    protected FeaturesProcessor featuresProcessor;


    @Override
    public void parse(LineBytesBuffer line, SparseItem item, long lineNo) {
        item.clear();
        valueBuffer.clear();
        int colNum = 0;
        for (int i = 0; i < line.size(); i++) {
            byte b = line.get(i);
            if (((char) b) != csvDelimiter()) {
                valueBuffer.append(b);
            } else {
                processColumn(item, colNum, valueBuffer);
                colNum++;
                valueBuffer.clear();
            }
        }
        processColumn(item, colNum, valueBuffer);
        if (item.id() == null) {
            item.id(Long.toString(lineNo));
        }
        finalize(item);
        featuresProcessor.finalize(item);
    }

    protected void finalize(SparseItem item) {

    }

    protected abstract char csvDelimiter();

    protected abstract void processColumn(SparseItem item, int colNum, LineBytesBuffer valueBuffer);

    @Inject
    public AbstractDelimiterSeparatedValuesFormat featruresProcessor(FeaturesProcessor featuresProcessor) {
        this.featuresProcessor = featuresProcessor;
        return this;
    }
}
