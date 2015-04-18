package io.scaledml.ftrl.outputformats;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinishCollectStatisticsListener {
    private static final Logger logger = LoggerFactory.getLogger(FinishCollectStatisticsListener.class);

    private int expectedFinishCollectEventsNum;
    private int finishCollectEvents = 0;

    private double totalLogLikelyhood = 0.;
    private long totalItems = 0;

    public synchronized void finishedCollectingStatistics(StatisticsCalculator collector) {
        totalItems += collector.itemNo();
        totalLogLikelyhood += collector.logLikelihood();
        finishCollectEvents++;
        if (finishCollectEvents >= expectedFinishCollectEventsNum) {
            logger.info("Total mean logloss: " + -totalLogLikelyhood / totalItems + " Total items: " + totalItems);
        }
    }

    @Inject
    public FinishCollectStatisticsListener expectedFinishCollectEventsNum(@Named("statsCollectors") int expectedFinishCollectEventsNum) {
        this.expectedFinishCollectEventsNum = expectedFinishCollectEventsNum;
        return this;
    }
}
