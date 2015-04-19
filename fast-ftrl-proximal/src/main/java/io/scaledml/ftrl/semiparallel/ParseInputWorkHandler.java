package io.scaledml.ftrl.semiparallel;

import com.google.inject.Inject;
import com.lmax.disruptor.WorkHandler;
import io.scaledml.core.SparseItem;
import io.scaledml.ftrl.disruptor.TwoPhaseEvent;
import io.scaledml.core.inputformats.InputFormat;


public class ParseInputWorkHandler implements WorkHandler<TwoPhaseEvent<SparseItem>> {
    InputFormat inputFormat;

    @Override
    public void onEvent(TwoPhaseEvent<SparseItem> event) throws Exception {
        inputFormat.parse(event.input(), event.output(), event.lineNo());
    }

    @Inject
    public ParseInputWorkHandler inputFormat(InputFormat format) {
        this.inputFormat = format;
        return this;
    }
}
