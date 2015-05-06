package io.scaledml.core;

import com.lmax.disruptor.EventFactory;
import io.scaledml.core.util.LineBytesBuffer;

public class TwoPhaseEvent<T> {
    public static <T> EventFactory<TwoPhaseEvent<T>> factory(EventFactory<T> outputFactory) {
        return () -> new TwoPhaseEvent<>(outputFactory.newInstance());
    }

    private final LineBytesBuffer input = new LineBytesBuffer();
    private long lineNo;
    private final T output;

    public TwoPhaseEvent(T output) {
        this.output = output;
    }

    public LineBytesBuffer input() {
        return input;
    }

    public T output() {
        return output;
    }

    public TwoPhaseEvent<T> lineNo(long lineNo) {
        this.lineNo = lineNo;
        return this;
    }

    public long lineNo() {
        return lineNo;
    }
}
