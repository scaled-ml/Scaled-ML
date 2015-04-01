package io.scaledml.ftrl.outputformats;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StatisticsCalculator implements OutputFormat {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsCalculator.class);
    private OutputFormat delegate;
    private FinishCollectStatisticsListener finishListener;
    private double logLikelyhood = 0.;
    private long itemNo = 0;
    private long nextItemNoToPrint = 1;

    public StatisticsCalculator() {
        logger.info("mean logloss\titems\tcurrent label\tcurrent prediction");
    }

    @Override
    public void emmit(SparseItem item, double prediction) {
        delegate.emmit(item, prediction);
        itemNo++;
        logLikelyhood += Math.log(Util.doublesEqual(1., item.label()) ?  prediction : 1 - prediction);
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
    public StatisticsCalculator delegate(@Named("delegate") OutputFormat delegate) {
        this.delegate = delegate;
        return this;
    }

    @Inject
    public StatisticsCalculator finishListener(FinishCollectStatisticsListener finishListener) {
        this.finishListener = finishListener;
        return this;
    }
}
