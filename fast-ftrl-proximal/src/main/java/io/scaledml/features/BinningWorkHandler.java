package io.scaledml.features;


import com.google.inject.Inject;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.core.SparseItem;
import io.scaledml.core.inputformats.InputFormat;
import io.scaledml.core.util.Util;
import io.scaledml.core.TwoPhaseEvent;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public class BinningWorkHandler implements WorkHandler<TwoPhaseEvent<SparseItem>> {
    private final SparseItem item = new SparseItem();
    private NumericalFeaturesStatistics statistics;
    private InputFormat format;


    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event) throws Exception {
        SparseItem outputItem = event.output();
        item.clear();
        outputItem.clear();
        format.parse(event.input(), item, event.lineNo());
        outputItem
                .copyCategoricalFeaturesFrom(item)
                .label(item.label())
                .id(item.id());
        Long2ObjectMap<Binning> binnings = statistics.binnings();
        for (int i = 0; i < item.numericalIndexes().size(); i++) {
            long numericalIndex = item.numericalIndexes().getLong(i);
            double numericalValue = item.numericalValues().getDouble(i);
            long binningIndex = Util.murmur().newHasher()
                    .putLong(numericalIndex)
                    .putDouble(binnings.get(numericalIndex).roundToPercentile(numericalValue))
                    .hash().asLong();
            outputItem.addCategoricalIndex(binningIndex);
        }
    }

    @Inject
    public BinningWorkHandler statistics(NumericalFeaturesStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    @Inject
    public BinningWorkHandler format(InputFormat format) {
        this.format = format;
        return this;
    }
}
