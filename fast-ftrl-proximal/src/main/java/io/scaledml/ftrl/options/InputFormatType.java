package io.scaledml.ftrl.options;


import io.scaledml.core.inputformats.BinaryInputFormat;
import io.scaledml.core.inputformats.CSVFormat;
import io.scaledml.core.inputformats.InputFormat;
import io.scaledml.core.inputformats.VowpalWabbitFormat;

public enum InputFormatType {
    vw(VowpalWabbitFormat.class),
    csv(CSVFormat.class),
    binary(BinaryInputFormat.class);
    public final Class<? extends InputFormat> formatClass;

    InputFormatType(Class<? extends InputFormat> formatClass) {
        this.formatClass = formatClass;
    }
}
