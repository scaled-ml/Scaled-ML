package io.scaledml.outputformats;


import io.scaledml.SparseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CollectStatisticsOutputFormat implements OutputFormat {
    private static final Logger logger = LoggerFactory.getLogger(CollectStatisticsOutputFormat.class);
    private OutputFormat delegate;
    double logLikelyhood = 0.;
    long itemNo = 0;
    long nextItemNoToPrint = 1;

    public CollectStatisticsOutputFormat() {
        logger.info("mean logloss\titems\tcurrent label\tcurrent prediction");
    }

    @Override
    public void emmit(SparseItem item, double prediction) {
        delegate.emmit(item, prediction);
        itemNo++;
        logLikelyhood += Math.log(item.label() - 0. > 0.9 ?  prediction : 1 - prediction);
        if (itemNo == nextItemNoToPrint) {
            nextItemNoToPrint *= 2;
            logger.info(-logLikelyhood / itemNo + "\t" + itemNo + "\t" +
                    item.label() + "\t" + prediction);
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("Total mean logloss: " + -logLikelyhood / itemNo + " Total items: " + itemNo);
        delegate.close();
    }

    public CollectStatisticsOutputFormat delegate(OutputFormat delegate) {
        this.delegate = delegate;
        return this;
    }
}
