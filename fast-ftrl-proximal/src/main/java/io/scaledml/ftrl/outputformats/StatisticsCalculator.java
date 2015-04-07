package io.scaledml.ftrl.outputformats;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StatisticsCalculator implements OutputFormat {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsCalculator.class);
    private OutputFormat delegate;
    private FinishCollectStatisticsListener finishListener;
    private double logLikelyhood = 0.;
    private double smoothLogLikelyhood = 0.;
    private double alfa = 1. / 10000.;
    private long itemNo = 0;
    private long nextItemNoToPrint = 1;

    public StatisticsCalculator() {
        logger.info("mean logloss\tsmooth logloss\titems\tcurrent label\tcurrent prediction\tfeatures number");
    }

    @Override
    public void emit(SparseItem item, double prediction) {
        delegate.emit(item, prediction);
        itemNo++;
        double itemLikelyhood = Math.log(Util.doublesEqual(1., item.label()) ? prediction : 1 - prediction);
        logLikelyhood += itemLikelyhood;
        smoothLogLikelyhood = smoothLogLikelyhood * (1. - alfa) + itemLikelyhood * alfa;
        if (itemNo == nextItemNoToPrint) {
            nextItemNoToPrint *= 2;
            logger.info(-logLikelyhood / itemNo + "\t" + ((1. / alfa) > itemNo ? -smoothLogLikelyhood : 0) +
                    "\t" + itemNo + "\t" + item.label() + "\t" + prediction + "\t" + item.indexes().size());
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
