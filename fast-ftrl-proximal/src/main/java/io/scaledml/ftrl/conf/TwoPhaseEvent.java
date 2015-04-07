package io.scaledml.ftrl.conf;

import com.lmax.disruptor.EventFactory;
import io.scaledml.ftrl.util.LineBytesBuffer;

public class TwoPhaseEvent<T> {
    public static <T> EventFactory<TwoPhaseEvent<T>> factory(EventFactory<T> outputFactory) {
        return () -> new TwoPhaseEvent<>(outputFactory.newInstance());
    }

    private final LineBytesBuffer input = new LineBytesBuffer();
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
}
