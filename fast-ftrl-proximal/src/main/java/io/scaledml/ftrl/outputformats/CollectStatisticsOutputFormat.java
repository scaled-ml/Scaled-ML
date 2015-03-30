package io.scaledml.ftrl.outputformats;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CollectStatisticsOutputFormat implements OutputFormat {
    private static final Logger logger = LoggerFactory.getLogger(CollectStatisticsOutputFormat.class);
    private OutputFormat delegate;
    private FinishCollectStatisticsListener finishListener;
    private double logLikelyhood = 0.;
    private long itemNo = 0;
    private long nextItemNoToPrint = 1;

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
        finishListener.finishedCollectingStatistics(this);
        // delegate can be shared among threads so it mus be closed separately
    }

    public double logLikelyhood() {
        return logLikelyhood;
    }

    public long itemNo() {
        return itemNo;
    }

    @Inject
    public CollectStatisticsOutputFormat delegate(@Named("delegate") OutputFormat delegate) {
        this.delegate = delegate;
        return this;
    }

    @Inject
    public CollectStatisticsOutputFormat finishListener(FinishCollectStatisticsListener finishListener) {
        this.finishListener = finishListener;
        return this;
    }
}
