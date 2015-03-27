package io.scaledml.semiparallel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.inputformats.InputFormat;
import io.scaledml.SparseItem;
import io.scaledml.io.LineBytesBuffer;


public class ParseInputWorkHandler implements WorkHandler<LineBytesBuffer> {
    InputFormat inputFormat;
    Disruptor<SparseItem> itemDisruptor;

    @Override
    public void onEvent(LineBytesBuffer event) throws Exception {
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
