package io.scaledml.features;

import com.google.inject.Inject;

import java.io.IOException;

public class FeatureEngineeringRunner {
    private FirstPassRunner firstPassRunner;
    private SecondPassRunner secondPassRunner;
    private NumericalFeaturesStatistics statistics;

    public void process() throws IOException {
        firstPassRunner.process();
        statistics.logFeaturesStatistics();
        secondPassRunner.process();
    }

    @Inject
    public FeatureEngineeringRunner firstPassRunner(FirstPassRunner firstPassRunner) {
        this.firstPassRunner = firstPassRunner;
        return this;
    }

    @Inject
    public FeatureEngineeringRunner secondPassRunner(SecondPassRunner secondPassRunner) {
        this.secondPassRunner = secondPassRunner;
        return this;
    }

    @Inject
    public FeatureEngineeringRunner statistics(NumericalFeaturesStatistics statistics) {
        this.statistics = statistics;
        return this;
    }
}
