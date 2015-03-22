package io.scaledml;


import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;

class ModelIncrement {

}
public class ParallelRun {
    Disruptor<ByteBuffer> inputDisruptor;
    Disruptor<ModelIncrement> incrementDisruptor;
}
