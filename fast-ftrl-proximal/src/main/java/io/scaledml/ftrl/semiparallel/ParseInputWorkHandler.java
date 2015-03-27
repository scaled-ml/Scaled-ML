package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.ftrl.inputformats.InputFormat;
import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.io.LineBytesBuffer;


public class ParseInputWorkHandler implements EventHandler<LineBytesBuffer> {
    InputFormat inputFormat;
    Disruptor<SparseItem> itemDisruptor;

    @Override
    public void onEvent(LineBytesBuffer event, long sequence, boolean endOfBatch) throws Exception {
        long cursor = itemDisruptor.getRingBuffer().next();
        SparseItem item = itemDisruptor.get(cursor);
        inputFormat.parse(event, item);
        itemDisruptor.getRingBuffer().publish(cursor);
    }

    @Inject
    public ParseInputWorkHandler inputFormat(InputFormat format) {
        this.inputFormat = format;
        return this;
    }
    @Inject
    public ParseInputWorkHandler itemDisruptor(@Named("secondDisruptor") Disruptor<SparseItem> itemDisruptor) {
        this.itemDisruptor = itemDisruptor;
        return this;
    }
}
