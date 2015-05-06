package io.scaledml.ftrl;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.core.BaseDisruptorRunner;
import io.scaledml.core.outputformats.OutputFormat;
import io.scaledml.core.TwoPhaseEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class FtrlProximalRunner extends BaseDisruptorRunner {
    private FtrlProximalModel model;
    private Path outputForModelPath;
    private OutputFormat outputFormat;

    protected void afterDisruptorProcessed() throws IOException {
        outputFormat.close();
        if (outputForModelPath != null) {
            FtrlProximalModel.saveModel(model, outputForModelPath);
        }
    }

    @Inject
    public FtrlProximalRunner disruptor(@Named("disruptor") Disruptor<? extends TwoPhaseEvent<?>> disruptor) {
        setDisruptor(disruptor);
        return this;
    }

    @Inject
    public FtrlProximalRunner model(FtrlProximalModel model) {
        this.model = model;
        return this;
    }

    @Inject
    public FtrlProximalRunner outputFormat(@Named("delegate") OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Inject
    public FtrlProximalRunner outputForModelPath(Optional<Path> outputForModelPath) {
        return outputForModelPath(outputForModelPath.orElse(null));
    }

    public FtrlProximalRunner outputForModelPath(Path outputForModelPath) {
        this.outputForModelPath = outputForModelPath;
        return this;
    }

}
